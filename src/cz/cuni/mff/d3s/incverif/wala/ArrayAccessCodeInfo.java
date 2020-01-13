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

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.ArrayID;


public class ArrayAccessCodeInfo
{
	public static Set<ArrayID> getArraysAccessedByInsn(CGNode mthNode, SSAArrayReferenceInstruction arrInsn, WALAContext walaCtx)
	{
		Set<ArrayID> arrayIDs = new HashSet<ArrayID>();
			
		try
		{
			String methodSig = mthNode.getMethod().getSignature();

			TypeReference arrayObjTypeRef = arrInsn.getElementType().getArrayTypeForElementType();
			
			String arrayClassName = WALAUtils.getTypeNameStr(arrayObjTypeRef.getName());

			int arrayDimensions = arrayObjTypeRef.getDimensionality();

			Set<AllocationSite> objectAllocSites = walaCtx.allocSitesData.getAllocSitesForObject(methodSig, arrInsn.getArrayRef(), arrayClassName);

			for (AllocationSite objAS : objectAllocSites) arrayIDs.add(new ArrayID(objAS, arrayDimensions));
		}
		catch (Exception ex) 
		{
			ex.printStackTrace(); 
		}
			
		return arrayIDs;
	}

}

