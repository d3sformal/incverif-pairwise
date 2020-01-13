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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ArrayClass;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;

import cz.cuni.mff.d3s.incverif.common.AllocationSite;


public class ThreadEscapeAnalysis
{
	public Set<AllocationSite> escapedAllocSites;


	public ThreadEscapeAnalysis()
	{
		escapedAllocSites = new HashSet<AllocationSite>();
	}

	public void analyzeProgram(WALAContext walaCtx) throws Exception
	{
		Set<PointerKey> escapeRoots = new HashSet<PointerKey>();

		HeapModel hm = walaCtx.pointerAnalysis.getHeapModel();

		// roots are (1) static fields and (2) instance fields of Thread objects

		// get all static fields in all classes
		
		for (IClass cls : walaCtx.classHierarchy)
		{
			for (IField fld : cls.getDeclaredStaticFields())
			{
				if (fld.getFieldTypeReference().isReferenceType())
				{
					escapeRoots.add(hm.getPointerKeyForStaticField(fld));
				}
			}
		}

		// get all instance fields in every Thread object

		for (IClass thSubCls : walaCtx.classHierarchy.computeSubClasses(TypeReference.JavaLangThread))
		{
			for (IMethod mth : thSubCls.getDeclaredMethods())
			{
				if (mth.isInit())
				{
					Set<CGNode> mthNodes = walaCtx.callGraph.getNodes(mth.getReference());

					for (CGNode node : mthNodes)
					{
						// "this" parameter
						escapeRoots.add(hm.getPointerKeyForLocal(node, 1));
					}
				}
			}
		}

		// get all objects transitively reachable from the escape roots (pointer keys) through fields

		Set<InstanceKey> escapedObjects = new HashSet<InstanceKey>();

		// phase 1: get abstract objects (instance keys) for escaping roots

		for (PointerKey rootPK : escapeRoots)
		{
			Iterable<InstanceKey> objColl = walaCtx.pointerAnalysis.getPointsToSet(rootPK);

			for (InstanceKey objIK : objColl)
			{
				escapedObjects.add(objIK);
			}
		}

		// phase 2+: add points-to sets of fields in escaped objects
			// continue until the fixpoint is reached
		
		while (true)
		{
			Set<InstanceKey> newObjects = new HashSet<InstanceKey>();

			for (InstanceKey objIK : escapedObjects)
			{
				IClass objType = objIK.getConcreteType();

				if (objType.isReferenceType())
				{
					if (objType.isArrayClass())
					{
						ArrayClass arrType = (ArrayClass) objType;

						if (arrType.getElementClass() != null)
						{
							PointerKey arrPK = hm.getPointerKeyForArrayContents(objIK);

							Iterable<InstanceKey> arrElemColl = walaCtx.pointerAnalysis.getPointsToSet(arrPK);

							for (InstanceKey arrElemIK : arrElemColl)
							{
								if ( ! escapedObjects.contains(arrElemIK) ) newObjects.add(arrElemIK);
							}
						}
					}
					else
					{
						for (IField fld : objType.getAllInstanceFields())
						{
							if (fld.getFieldTypeReference().isReferenceType())
							{
								PointerKey fldPK = hm.getPointerKeyForInstanceField(objIK, fld);

								Iterable<InstanceKey> fldValColl = walaCtx.pointerAnalysis.getPointsToSet(fldPK);

								for (InstanceKey fldValIK : fldValColl)
								{
									if ( ! escapedObjects.contains(fldValIK) ) newObjects.add(fldValIK);
								}
							}
						}
					}
				}
			}

			if (newObjects.isEmpty()) break;

			escapedObjects.addAll(newObjects);
		}

		// collect the respective allocation sites

		for (InstanceKey objIK : escapedObjects)
		{
			if (objIK instanceof AllocationSiteInNode)
			{
				AllocationSiteInNode objAllocIK = (AllocationSiteInNode) objIK;

				AllocationSite objAS = WALAUtils.getAllocationSite(objAllocIK, walaCtx);

				escapedAllocSites.add(objAS);
			}
		}
	}
}

