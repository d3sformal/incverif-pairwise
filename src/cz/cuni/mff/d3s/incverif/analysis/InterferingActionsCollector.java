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
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.util.graph.Graph;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.ClassName;
import cz.cuni.mff.d3s.incverif.common.CodeBlockBoundary;
import cz.cuni.mff.d3s.incverif.common.FieldAccess;
import cz.cuni.mff.d3s.incverif.common.ArrayObjectAccess;
import cz.cuni.mff.d3s.incverif.common.SynchEventExec;
import cz.cuni.mff.d3s.incverif.common.SynchEventType;
import cz.cuni.mff.d3s.incverif.wala.WALAUtils;
import cz.cuni.mff.d3s.incverif.wala.WALAContext;


public class InterferingActionsCollector
{
	public WALAContext walaCtx;

	public FieldAccessCollector fieldAccessColl;
	public ArrayObjectAccessCollector arrayObjAccessColl;
	public SynchEventCollector synchEventColl;

	public VariableUpdateLocationsCollector varUpdateLocsColl;
	public MethodInvokeLocationsCollector mthInvokeLocsColl;


	public InterferingActionsCollector(WALAContext wc, FieldAccessCollector fldAcc, ArrayObjectAccessCollector arrObjAcc, SynchEventCollector synchEv, VariableUpdateLocationsCollector varUpdLocs, MethodInvokeLocationsCollector mthInvLocs)
	{
		this.walaCtx = wc;

		this.fieldAccessColl = fldAcc;
		this.arrayObjAccessColl = arrObjAcc;
		this.synchEventColl = synchEv;

		this.varUpdateLocsColl = varUpdLocs;
		this.mthInvokeLocsColl = mthInvLocs;
	}

	public Set<CodeBlockBoundary> getAffectedCodeBlockBoundaries() throws Exception
	{
		Set<CodeBlockBoundary> codeBlocks = new HashSet<CodeBlockBoundary>();

		// field accesses

		for (FieldAccess fr : fieldAccessColl.fieldReads)
		{
			ProgramPoint startPP = fr.progPoint;
			ProgramPoint endPP = fr.progPoint;

			// expand to cover also usage of the read field value (transitively) by subsequent instructions (over-approximation)
			// this includes operands for conditional branching instructions within the respective code block
			LinkedList<ProgramPoint> consumingPPs = WALAUtils.findResultsConsumingEndBoundaryForBytecodeInsn(endPP, true, walaCtx);
			endPP = consumingPPs.getFirst();

			// expand to cover also loading of input operands for the subsequent instructions that use the read value (over-approximation)
			List<ProgramPoint> loadPPs = new LinkedList<ProgramPoint>();
			loadPPs.add(WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx));
			for (ProgramPoint consPP : consumingPPs) loadPPs.add(WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(consPP, walaCtx));
			startPP = Collections.min(loadPPs);

			CodeBlockBoundary frCB = new CodeBlockBoundary(startPP, endPP);
			codeBlocks.add(frCB);
		}

		for (FieldAccess fw : fieldAccessColl.fieldWrites)
		{
			ProgramPoint startPP = fw.progPoint;
			ProgramPoint endPP = fw.progPoint;

			// expand to cover also loading of the target heap object and the new value
			startPP = WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx);

			CodeBlockBoundary fwCB = new CodeBlockBoundary(startPP, endPP);
			codeBlocks.add(fwCB);
		}

		// array object accesses

		for (ArrayObjectAccess aor : arrayObjAccessColl.arrayReads)
		{
			ProgramPoint startPP = aor.progPoint;
			ProgramPoint endPP = aor.progPoint;

			// expand to cover also usage of the read array element value (transitively) by subsequent instructions (over-approximation)
			// this includes operands for conditional branching instructions within the respective code block
			LinkedList<ProgramPoint> consumingPPs = WALAUtils.findResultsConsumingEndBoundaryForBytecodeInsn(endPP, true, walaCtx);
			endPP = consumingPPs.getFirst();

			// expand to cover also loading of input operands for the subsequent instructions that use the read value (over-approximation)
			List<ProgramPoint> loadPPs = new LinkedList<ProgramPoint>();
			loadPPs.add(WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx));
			for (ProgramPoint consPP : consumingPPs) loadPPs.add(WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(consPP, walaCtx));
			startPP = Collections.min(loadPPs);

			CodeBlockBoundary aorCB = new CodeBlockBoundary(startPP, endPP);
			codeBlocks.add(aorCB);
		}

		for (ArrayObjectAccess aow : arrayObjAccessColl.arrayWrites)
		{
			ProgramPoint startPP = aow.progPoint;
			ProgramPoint endPP = aow.progPoint;

			// expand to cover also loading of the target heap array object, the element index, and the new value
			startPP = WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx);

			CodeBlockBoundary aowCB = new CodeBlockBoundary(startPP, endPP);
			codeBlocks.add(aowCB);
		}

		// synchronization events

		for (SynchEventExec sev : synchEventColl.synchEvents)
		{
			SynchEventType sevType = sev.targetEvent.eventType;

			if (sevType == SynchEventType.LOCK_ANYOBJECT)
			{
				SynchEventExec lockEv = sev;
				SynchEventExec unlockEv = synchEventColl.findMatchingUnlockEvent(lockEv, walaCtx);

				ProgramPoint startPP = lockEv.progPoint;
				ProgramPoint endPP = unlockEv.progPoint;

				// expand to cover also loading of the monitor (lock) object
					// includes also the sequence of three bytecode instructions (load ; dup ; store) often produced by the javac compiler before the "monitorenter" bytecode instruction
				startPP = WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx);

				CodeBlockBoundary sevCB = new CodeBlockBoundary(startPP, endPP);
				codeBlocks.add(sevCB);
			}
			else if (sevType == SynchEventType.LOCK_METHODRCV) 
			{
				ProgramPoint startPP = sev.progPoint;
				ProgramPoint endPP = sev.progPoint;
	
				// expand to cover also usage (transitively) of the return value (if there is one) by subsequent instructions (over-approximation)
				// this includes operands for conditional branching instructions within the respective code block
				LinkedList<ProgramPoint> consumingPPs = WALAUtils.findResultsConsumingEndBoundaryForBytecodeInsn(endPP, true, walaCtx);
				endPP = consumingPPs.getFirst();

				// expand to cover also loading of (1) the method call receiver object and (2) input operands for the subsequent instructions that use the returned value (if there is one)
				List<ProgramPoint> loadPPs = new LinkedList<ProgramPoint>();
				loadPPs.add(WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx));
				for (ProgramPoint consPP : consumingPPs) loadPPs.add(WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(consPP, walaCtx));
				startPP = Collections.min(loadPPs);

				CodeBlockBoundary sevCB = new CodeBlockBoundary(startPP, endPP);
				codeBlocks.add(sevCB);
			}
			else if ((sevType == SynchEventType.WAIT) || (sevType == SynchEventType.NOTIFY) || (sevType == SynchEventType.THSTART) || (sevType == SynchEventType.THJOIN))
			{
				// all methods covered here do not have a return value

				ProgramPoint startPP = sev.progPoint;
				ProgramPoint endPP = sev.progPoint;
	
				// expand to cover also loading of the method call receiver object
				startPP = WALAUtils.findOperandsLoadingStartBoundaryForBytecodeInsn(startPP, walaCtx);

				CodeBlockBoundary sevCB = new CodeBlockBoundary(startPP, endPP);
				codeBlocks.add(sevCB);
			}
		}

		return codeBlocks;
	}

	private ProgramPoint findValueUsageEndBoundaryForReadAccess(ProgramPoint readPP)
	{
		ProgramPoint updatePP = this.varUpdateLocsColl.findNearestUpdateLocationAfter(readPP);

		ProgramPoint invokePP = this.mthInvokeLocsColl.findNearestInvokeLocationAfter(readPP);

		// this really should not happen for valid bytecode
		if ((updatePP == null) && (invokePP == null)) return readPP;

		// there is no write (store/putfield) after the read
		if (updatePP == null) return invokePP;
	
		// there is no method call without a return value after the read
		if (invokePP == null) return updatePP;

		// compare both program points to get the one closer to the read

		int cmpRes = updatePP.compareTo(invokePP);

		if (cmpRes > 0) return invokePP;
		else return updatePP;
	}

}

