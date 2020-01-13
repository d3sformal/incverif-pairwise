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

import java.util.Set;
import java.util.HashSet;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.FieldID;


public class FieldAccessCodeInfo
{
	public static Set<FieldID> getFieldsForInsn(CGNode mthNode, SSAFieldAccessInstruction faInsn, WALAContext walaCtx)
	{
		Set<FieldID> fields = new HashSet<FieldID>();
			
		try
		{
			String fieldName = faInsn.getDeclaredField().getName().toUnicodeString();
			
			String className = WALAUtils.getDeclaringClassNameForField(faInsn.getDeclaredField(), walaCtx);
			
			String methodSig = mthNode.getMethod().getSignature();

			Set<AllocationSite> objectAllocSites = walaCtx.allocSitesData.getAllocSitesForObject(methodSig, faInsn.getRef(), className);
			
			for (AllocationSite objAS : objectAllocSites) fields.add(new FieldID(objAS, fieldName));
		}
		catch (Exception ex) 
		{
			ex.printStackTrace(); 
		}
			
		return fields;
	}
}

