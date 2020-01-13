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
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.graph.Graph;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.ClassName;
import cz.cuni.mff.d3s.incverif.common.ArrayID;
import cz.cuni.mff.d3s.incverif.common.ArrayObjectAccess;
import cz.cuni.mff.d3s.incverif.common.Utils;
import cz.cuni.mff.d3s.incverif.wala.WALAUtils;
import cz.cuni.mff.d3s.incverif.wala.WALAContext;
import cz.cuni.mff.d3s.incverif.wala.AllocationSitesData;
import cz.cuni.mff.d3s.incverif.wala.ArrayAccessCodeInfo;


public class ArrayObjectAccessCollector
{
	// set of array objects accesses for reading (xALOAD) 
	public Set<ArrayObjectAccess> arrayReads; 
	
	// set of array objects accesses for writing (xASTORE) 
	public Set<ArrayObjectAccess> arrayWrites;


	public ArrayObjectAccessCollector()
	{
		arrayReads = new HashSet<ArrayObjectAccess>();
		arrayWrites = new HashSet<ArrayObjectAccess>();
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

				if (insn instanceof SSAArrayReferenceInstruction) 
				{
					SSAArrayReferenceInstruction arrInsn = (SSAArrayReferenceInstruction) insn;
						
					try
					{
						int insnBcPos = WALAUtils.getInsnBytecodePos(mthNode, insnIndex);
						int insnBcIndex = WALAUtils.getInsnBytecodeIndex(mthNode, insnBcPos, walaCtx);
						ProgramPoint insnPP = new ProgramPoint(mthNode.getMethod().getSignature(), insnIndex, insnBcPos, insnBcIndex);

						Set<ArrayID> arrayIDs = ArrayAccessCodeInfo.getArraysAccessedByInsn(mthNode, arrInsn, walaCtx);

						if (arrInsn instanceof SSAArrayLoadInstruction)
						{
							for (ArrayID aID : arrayIDs)
							{
								ArrayObjectAccess aoAcc = new ArrayObjectAccess(aID, insnPP);

								arrayReads.add(aoAcc);
							}
						}

						if (arrInsn instanceof SSAArrayStoreInstruction)
						{
							for (ArrayID aID : arrayIDs)
							{
								ArrayObjectAccess aoAcc = new ArrayObjectAccess(aID, insnPP);

								arrayWrites.add(aoAcc);
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
		for (Iterator<ArrayObjectAccess> arIt = arrayReads.iterator(); arIt.hasNext(); )
		{
			ArrayObjectAccess aoAcc = arIt.next();

			if (Utils.isThreadLocalObject(aoAcc.targetArray.heapObjectID, sharedObjects)) arIt.remove();
		}
		
		for (Iterator<ArrayObjectAccess> awIt = arrayWrites.iterator(); awIt.hasNext(); )
		{
			ArrayObjectAccess aoAcc = awIt.next();

			if (Utils.isThreadLocalObject(aoAcc.targetArray.heapObjectID, sharedObjects)) awIt.remove();
		}
	}

}

