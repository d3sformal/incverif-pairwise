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
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.SortedMap;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.graph.Graph;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.ClassName;
import cz.cuni.mff.d3s.incverif.common.SynchEventID;
import cz.cuni.mff.d3s.incverif.common.SynchEventExec;
import cz.cuni.mff.d3s.incverif.common.SynchEventType;
import cz.cuni.mff.d3s.incverif.common.Utils;
import cz.cuni.mff.d3s.incverif.wala.WALAContext;
import cz.cuni.mff.d3s.incverif.wala.WALAUtils;
import cz.cuni.mff.d3s.incverif.wala.AllocationSitesData;
import cz.cuni.mff.d3s.incverif.wala.SynchEventCodeInfo;


public class SynchEventCollector
{
	public Set<SynchEventExec> synchEvents;

	// map from method signatures to events sorted by program points
	private Map<String, SortedMap<ProgramPoint, Set<SynchEventExec>>> mthSig2EventsByPP;


	public SynchEventCollector()
	{
		synchEvents = new HashSet<SynchEventExec>();

		mthSig2EventsByPP = new HashMap<String, SortedMap<ProgramPoint, Set<SynchEventExec>>>();
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

				String mthSig = mthNode.getMethod().getSignature();

				int insnBcPos = WALAUtils.getInsnBytecodePos(mthNode, insnIndex);
				int insnBcIndex = WALAUtils.getInsnBytecodeIndex(mthNode, insnBcPos, walaCtx);
				ProgramPoint insnPP = new ProgramPoint(mthSig, insnIndex, insnBcPos, insnBcIndex);

				Set<SynchEventID> eventIDs = SynchEventCodeInfo.getSynchEventsForInsn(mthNode, methodIR, insn, walaCtx);

				for (SynchEventID evID : eventIDs)
				{
					SynchEventExec evExec = new SynchEventExec(evID, insnPP);

					synchEvents.add(evExec);

					SortedMap<ProgramPoint, Set<SynchEventExec>> mthEvents = mthSig2EventsByPP.get(mthSig);

					if (mthEvents == null)
					{
						mthEvents = new TreeMap<ProgramPoint, Set<SynchEventExec>>();
						mthSig2EventsByPP.put(mthSig, mthEvents);
					}

					Set<SynchEventExec> ppEvents = mthEvents.get(insnPP);

					if (ppEvents == null)
					{
						ppEvents = new HashSet<SynchEventExec>();
						mthEvents.put(insnPP, ppEvents);
					}

					ppEvents.add(evExec);
				}
			}
		}
	}

	public void dropThreadLocalEvents(Set<AllocationSite> sharedObjects)
	{
		for (Iterator<SynchEventExec> evIt = synchEvents.iterator(); evIt.hasNext(); )
		{
			SynchEventExec evExec = evIt.next();

			if (Utils.isThreadLocalObject(evExec.targetEvent.tgtObjectID, sharedObjects)) evIt.remove();
		}
	}

	public SynchEventExec findMatchingUnlockEvent(SynchEventExec inLockEv, WALAContext walaCtx) throws Exception
	{
		// select lock and unlock events from the same method
			// ignore unlock events in exception handlers (monitorexit instructions preceded by "goto ; astore ; aload")

		SortedMap<ProgramPoint, Set<SynchEventExec>> mthEvents = mthSig2EventsByPP.get(inLockEv.progPoint.methodSig);

		SortedMap<ProgramPoint, Set<SynchEventExec>> mthLockUnlockEvents = new TreeMap<ProgramPoint, Set<SynchEventExec>>();

		for (Set<SynchEventExec> evSet : mthEvents.values())
		{
			for (SynchEventExec evExec : evSet)
			{
				if (evExec.targetEvent.eventType.isLockOrUnlock())
				{
					if (WALAUtils.isMonitorExitBytecodeInsnWithinExceptionHandler(evExec.progPoint, walaCtx)) continue;

					Set<SynchEventExec> ppEvents = mthLockUnlockEvents.get(evExec.progPoint);

					if (ppEvents == null)
					{
						ppEvents = new HashSet<SynchEventExec>();
						mthLockUnlockEvents.put(evExec.progPoint, ppEvents);
					}

					ppEvents.add(evExec);
				}
			}
		}

		// we need to consider nested synchronized blocks (count the opening LOCK events and closing UNLOCK events)

		int lockDepth = 0;

		for (Map.Entry<ProgramPoint, Set<SynchEventExec>> me : mthLockUnlockEvents.entrySet())
		{
			ProgramPoint pp = me.getKey();
			Set<SynchEventExec> ppEvents = me.getValue();

			// we have to ignore all events that precede the input lock event in the sequence of bytecode instructions
			if (pp.compareTo(inLockEv.progPoint) < 0) continue;

			// check if we hit the input lock event
			if (ppEvents.contains(inLockEv))
			{
				lockDepth = 1;
				// we cannot move to the next program point immediately, since both the respective LOCK and UNLOCK events are located at the same program point in the case of synchronized methods
			}

			SynchEventExec candidateEvExec = null;

			// we have to process all events associated with the current program point atomically with respect to the check for matching unlock events
			for (SynchEventExec evExec : ppEvents)
			{
				// the input synch event cannot be taken into account twice
				if (evExec.equals(inLockEv)) continue;

				if (evExec.targetEvent.eventType.isLock()) lockDepth++;
				if (evExec.targetEvent.eventType.isUnlock()) lockDepth--;

				if (lockDepth == 0) candidateEvExec = evExec;
				else candidateEvExec = null;
			}

			// we have found the matching unlock event
			if ((lockDepth == 0) && (candidateEvExec != null) && candidateEvExec.targetEvent.eventType.isUnlock()) return candidateEvExec;
		}

		// this really should not happen
		// in program code, synchronization blocks syntactically define matching pairs of LOCK and UNLOCK events
		return null;
	}

}

