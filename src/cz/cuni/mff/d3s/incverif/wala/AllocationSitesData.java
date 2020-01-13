/*
 * Copyright (C) 2020, Charles University.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.cuni.mff.d3s.incverif.wala;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.Iterator;
import java.util.Collection;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.classLoader.IField;

import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.LocalVarID;


public class AllocationSitesData
{
	// map from class name to allocation sites for objects of the class
	private Map<String, Set<AllocationSite>> clsName2AllocSites;

	// map from local variable to allocation sites (points-to set)
	private Map<LocalVarID, Set<AllocationSite>> localVar2AllocSites;
	
	// map from allocation site (object) to local variables pointing to it
	private Map<AllocationSite, Set<LocalVarID>> allocSite2PointingVars;
	

	public AllocationSitesData()
	{
		clsName2AllocSites = new HashMap<String, Set<AllocationSite>>();
		localVar2AllocSites = new HashMap<LocalVarID, Set<AllocationSite>>();
		allocSite2PointingVars = new HashMap<AllocationSite, Set<LocalVarID>>();
	}

	public void initializeData(WALAContext walaCtx) throws Exception
	{
		collectAllocationSites(walaCtx);

		computeAliasingInformation();
	}
	
	public void addAllocSiteForClass(String className, AllocationSite allocSite)
	{
		Set<AllocationSite> sites = clsName2AllocSites.get(className);
		
		if (sites == null)
		{
			sites = new HashSet<AllocationSite>();
			clsName2AllocSites.put(className, sites);
		}
		
		sites.add(allocSite);
	}
	
	public Set<AllocationSite> getAllocSitesForClass(String className)
	{
		return clsName2AllocSites.get(className);
	}
	
	public void addAllocSiteForLocalVar(LocalVarID lv, AllocationSite allocSite)
	{
		Set<AllocationSite> sites = localVar2AllocSites.get(lv);
		
		if (sites == null)
		{
			sites = new HashSet<AllocationSite>();
			localVar2AllocSites.put(lv, sites);
		}
		
		sites.add(allocSite);
	}
	
	public Set<AllocationSite> getAllocSitesForLocalVar(LocalVarID lv)
	{
		return localVar2AllocSites.get(lv);
	}
	
	public Set<AllocationSite> getAllocSitesForLocalVar(String methodSig, int localVarNo)
	{
		LocalVarID lv = new LocalVarID(methodSig, localVarNo);
		
		return localVar2AllocSites.get(lv);
	}	

	public Set<AllocationSite> getAllocSitesForObject(String curMethodSig, int localVarNo, String objDeclClassName)
	{
		Set<AllocationSite> objectAllocSites = new HashSet<AllocationSite>();

		// check results of pointer analysis
		Set<AllocationSite> allocSitesLV = getAllocSitesForLocalVar(curMethodSig, localVarNo);

		if (allocSitesLV != null)
		{
			objectAllocSites.addAll(allocSitesLV);
		}
		else
		{
			// we do not have results of pointer analysis (it is a static field or a static method)
			objectAllocSites.add(WALAUtils.getAllocationSiteForClass(objDeclClassName));
		}

		return objectAllocSites;
	}

	private void collectAllocationSites(WALAContext walaCtx) throws Exception
	{
		// we cannot iterate directly because concurrent modification exception is thrown in that case
		List<InstanceKey> instKeys = new ArrayList<InstanceKey>();
		instKeys.addAll(walaCtx.pointerAnalysis.getInstanceKeys());
		
		for (InstanceKey ik : instKeys)
		{
			if (ik instanceof AllocationSiteInNode)
			{
				AllocationSiteInNode allocKey = (AllocationSiteInNode) ik;
				
				AllocationSite allocSite = WALAUtils.getAllocationSite(allocKey, walaCtx);
				
				String objClassName = WALAUtils.getClassName(allocKey.getConcreteType(), walaCtx);
				
				// static type of the abstract heap object
				addAllocSiteForClass(objClassName, allocSite);
				
				Iterator<Object> ofIt = walaCtx.pointerAnalysis.getHeapGraph().getSuccNodes(ik);
				
				// loop over fields
				while (ofIt.hasNext())
				{
					Object pk = ofIt.next();
					
					IField field = null;
					
					if (pk instanceof InstanceFieldKey) field = ((InstanceFieldKey) pk).getField();
					if (pk instanceof StaticFieldKey) field = ((StaticFieldKey) pk).getField();
					
					// get the class which truly declares the given field
					// it may be an arbitrary superclass of "objClassName" 
					String fieldDeclClsName = WALAUtils.getDeclaringClassNameForField(field, walaCtx);
			
					// other kind of field (pointer key)
					if (fieldDeclClsName == null) continue;
					
					addAllocSiteForClass(fieldDeclClsName, allocSite);
				}
			}
		}
	
		Iterable<PointerKey> pkColl = walaCtx.pointerAnalysis.getPointerKeys();

		for (PointerKey pk : pkColl)
		{
			if (pk instanceof LocalPointerKey)
			{	
				CGNode mthNode = ((LocalPointerKey) pk).getNode();
				int localVarNo = ((LocalPointerKey) pk).getValueNumber();

				String mthSignature = mthNode.getMethod().getSignature();
				
				LocalVarID lv = new LocalVarID(mthSignature, localVarNo);
				
				Iterator<Object> asIt = walaCtx.pointerAnalysis.getHeapGraph().getSuccNodes(pk);

				while (asIt.hasNext())
				{
					Object ik = asIt.next();
					
					if (ik instanceof AllocationSiteInNode)
					{
						AllocationSiteInNode allocKey = (AllocationSiteInNode) ik;
						
						String objClassName = WALAUtils.getClassName(allocKey.getConcreteType(), walaCtx);
	
						AllocationSite allocSite = WALAUtils.getAllocationSite(allocKey, walaCtx);
						
						addAllocSiteForLocalVar(lv, allocSite);
						
						addAllocSiteForClass(objClassName, allocSite);
					}
				}
			}		
		}
	}
	
	private void computeAliasingInformation()
	{
		for (LocalVarID lv : localVar2AllocSites.keySet())
		{
			Set<AllocationSite> sites = localVar2AllocSites.get(lv);
			
			for (AllocationSite as : sites)
			{
				Set<LocalVarID> pointVars = allocSite2PointingVars.get(as);
				
				if (pointVars == null)
				{
					pointVars = new HashSet<LocalVarID>();
					allocSite2PointingVars.put(as, pointVars);
				}
				
				pointVars.add(lv);
			}
		}		
	}
}

