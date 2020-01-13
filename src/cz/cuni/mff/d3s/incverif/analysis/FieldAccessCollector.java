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
package cz.cuni.mff.d3s.incverif.analysis;

import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.graph.Graph;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.ClassName;
import cz.cuni.mff.d3s.incverif.common.FieldID;
import cz.cuni.mff.d3s.incverif.common.FieldAccess;
import cz.cuni.mff.d3s.incverif.common.Utils;
import cz.cuni.mff.d3s.incverif.wala.WALAContext;
import cz.cuni.mff.d3s.incverif.wala.WALAUtils;
import cz.cuni.mff.d3s.incverif.wala.AllocationSitesData;
import cz.cuni.mff.d3s.incverif.wala.FieldAccessCodeInfo;


public class FieldAccessCollector
{
	// set of field read accesses (getfield, getstatic)
	public Set<FieldAccess> fieldReads; 
	
	// set of field write accesses (putfield, putstatic)
	public Set<FieldAccess> fieldWrites;


	public FieldAccessCollector()
	{
		fieldReads = new HashSet<FieldAccess>();
		fieldWrites = new HashSet<FieldAccess>();
	}


	public void analyzeProgram(WALAContext walaCtx) throws Exception
	{
		for (BasicBlockInContext<IExplodedBasicBlock> bb : walaCtx.interprocCFG)
		{
			CGNode mthNode = bb.getNode();

			if (WALAUtils.isSyntheticMethod(mthNode.getMethod())) continue;

			IR methodIR = mthNode.getIR();
				
			if (methodIR == null) continue;

			int firstInsnIdx = bb.getFirstInstructionIndex();
			int lastInsnIdx = bb.getLastInstructionIndex();
   
			// basic block without instructions
			if ((firstInsnIdx < 0) || (lastInsnIdx < 0)) continue;

			SSAInstruction[] instructions = methodIR.getInstructions();

			for (int insnIndex = firstInsnIdx; insnIndex <= lastInsnIdx; insnIndex++) 
			{
				SSAInstruction insn = instructions[insnIndex];

				if (insn instanceof SSAFieldAccessInstruction) 
				{
					SSAFieldAccessInstruction faInsn = (SSAFieldAccessInstruction) insn;
						
					try
					{
						int insnBcPos = WALAUtils.getInsnBytecodePos(mthNode, insnIndex);
						int insnBcIndex = WALAUtils.getInsnBytecodeIndex(mthNode, insnBcPos, walaCtx);
						ProgramPoint insnPP = new ProgramPoint(mthNode.getMethod().getSignature(), insnIndex, insnBcPos, insnBcIndex);

						Set<FieldID> fieldIDs = FieldAccessCodeInfo.getFieldsForInsn(mthNode, faInsn, walaCtx);

						if (faInsn instanceof SSAGetInstruction)
						{
							for (FieldID fID : fieldIDs)
							{
								FieldAccess fAcc = new FieldAccess(fID, insnPP);

								fieldReads.add(fAcc);
							}
						}

						if (faInsn instanceof SSAPutInstruction)
						{
							for (FieldID fID : fieldIDs)
							{
								FieldAccess fAcc = new FieldAccess(fID, insnPP);

								fieldWrites.add(fAcc);
							}
						}
					}
					catch (Exception ex) { ex.printStackTrace(); }						
				}
			}
		}
	}

	public void dropThreadLocalAccesses(Set<AllocationSite> sharedObjects)
	{
		for (Iterator<FieldAccess> frIt = fieldReads.iterator(); frIt.hasNext(); )
		{
			FieldAccess fAcc = frIt.next();

			if (Utils.isThreadLocalObject(fAcc.targetField.tgtObjectID, sharedObjects)) frIt.remove();
		}
		
		for (Iterator<FieldAccess> fwIt = fieldWrites.iterator(); fwIt.hasNext(); )
		{
			FieldAccess fAcc = fwIt.next();

			if (Utils.isThreadLocalObject(fAcc.targetField.tgtObjectID, sharedObjects)) fwIt.remove();
		}
	}

}

