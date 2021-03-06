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
package cz.cuni.mff.d3s.incverif.pairwise;

import java.util.List;
import java.util.Stack;
import java.util.Collections;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.CodeBlockBoundary;


public class ThreadExecutionMonitor extends ListenerAdapter
{
	private Config config;

	// thread with modified code
	private int thModifiedID;

	// encapsulates two program code locations (points) that represent boundaries of the modified code
	private CodeBlockBoundary modifiedCodeBoundary;

	// other thread forming the given pair
	private int thOtherID;

	// current transition
	// includes the current execution state of thread with modified code
	private TransitionInfo curTr;

	// stack of transitions on the current execution trace
	private Stack<TransitionInfo> curTraceTrs;

	// the maximum thread ID observed during the run of JPF
	private int maxThreadID;

	// signature of the entrypoint method (run) for the modified thread
	private String thModifiedEntryMethodSig;


	public ThreadExecutionMonitor(Config cfg, int tmid, CodeBlockBoundary mcbb, int toid)
	{
		this.config = cfg;

		this.thModifiedID = tmid;
		this.modifiedCodeBoundary = mcbb;

		this.thOtherID = toid;

		this.curTraceTrs = new Stack<TransitionInfo>();

		this.maxThreadID = 0;
	}

	public void searchStarted(Search search)
	{
		// we need to create and use a special root transition so that backtracking works properly
		TransitionInfo rootTr = new TransitionInfo(ExecState.BEFOREFIRST);
		curTraceTrs.push(rootTr);

		curTr = new TransitionInfo(ExecState.BEFOREFIRST);

		// main thread needs to be handled in a special way
		if (thModifiedID == 0) 
		{
			this.thModifiedEntryMethodSig = search.getVM().getCurrentApplicationContext().getMainClassName() + ".main([Ljava/lang/String;)V";
		}

		//System.out.println("[DEBUG PP] ThreadExecutionMonitor (search start): -> before first (" + curTr.state.ordinal() + ")");
	}
	
	public void stateAdvanced(Search search) 
	{
		TransitionInfo newTr = new TransitionInfo(curTr.state);

		curTraceTrs.push(curTr);

		curTr = newTr;
	}
	
	public void stateBacktracked(Search search) 
	{
		curTraceTrs.pop();

		TransitionInfo prevTr = curTraceTrs.peek();

		/*
		if (curTr.state != prevTr.state)
		{
			System.out.println("[DEBUG PP] ThreadExecutionMonitor (backtracking): updated state = " + prevTr.state.ordinal());
		}
		*/

		curTr = new TransitionInfo(prevTr.state);
	}

	public void instructionExecuted(VM vm, ThreadInfo curTh, Instruction nextInsn, Instruction execInsn)
	{
		if (curTh.isFirstStepInsn()) return;

		// we care just about the modified thread
		if (curTh.getId() != thModifiedID) return;

		if ( ! modifiedCodeBoundary.getMethodSignature().equals(execInsn.getMethodInfo().getFullName()) ) return; 

		int thModifiedCurInsnBcPos = execInsn.getPosition();

		if (curTr.state == ExecState.BEFOREFIRST)
		{
			if (thModifiedCurInsnBcPos == modifiedCodeBoundary.startLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): before first (" + ExecState.BEFOREFIRST.ordinal() + ") -> entering (" + ExecState.ENTERING.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.ENTERING;

				enableThreadChoicesForPossiblyConcurrentEvents(curTh, execInsn, vm);

				return;
			}
			
			// this may happen in the case of jumps directly into the bytecode range
			if ( (thModifiedCurInsnBcPos > modifiedCodeBoundary.startLoc.insnBcPos) && (thModifiedCurInsnBcPos < modifiedCodeBoundary.endLoc.insnBcPos) )
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): before first (" + ExecState.BEFOREFIRST.ordinal() + ") -> inside (" + ExecState.INSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.INSIDE;

				enableThreadChoicesForPossiblyConcurrentEvents(curTh, execInsn, vm);

				return;
			}
		}

		if (curTr.state == ExecState.ENTERING)
		{
			if (thModifiedCurInsnBcPos != modifiedCodeBoundary.startLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): entering (" + ExecState.ENTERING.ordinal() + ") -> inside (" + ExecState.INSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.INSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.INSIDE)
		{
			if (thModifiedCurInsnBcPos == modifiedCodeBoundary.endLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): inside (" + ExecState.INSIDE.ordinal() + ") -> exiting (" + ExecState.EXITING.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.EXITING;

				return;
			}

			// this may happen in the case of jumps directly out of the bytecode range
			if ( (thModifiedCurInsnBcPos > modifiedCodeBoundary.endLoc.insnBcPos) || (thModifiedCurInsnBcPos < modifiedCodeBoundary.startLoc.insnBcPos) )
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): inside (" + ExecState.INSIDE.ordinal() + ") -> outside (" + ExecState.OUTSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.OUTSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.EXITING)
		{
			if (thModifiedCurInsnBcPos != modifiedCodeBoundary.endLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): exiting (" + ExecState.EXITING.ordinal() + ") -> outside (" + ExecState.OUTSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.OUTSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.OUTSIDE)
		{
			if (thModifiedCurInsnBcPos == modifiedCodeBoundary.startLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): outside (" + ExecState.OUTSIDE.ordinal() + ") -> entering (" + ExecState.ENTERING.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.ENTERING;
	
				enableThreadChoicesForPossiblyConcurrentEvents(curTh, execInsn, vm);

				return;
			}

			// this may happen in the case of jumps directly into the bytecode range
			if ( (thModifiedCurInsnBcPos > modifiedCodeBoundary.startLoc.insnBcPos) && (thModifiedCurInsnBcPos < modifiedCodeBoundary.endLoc.insnBcPos) )
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): outside (" + ExecState.OUTSIDE.ordinal() + ") -> inside (" + ExecState.INSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", execInsn = " + execInsn.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.INSIDE;

				enableThreadChoicesForPossiblyConcurrentEvents(curTh, execInsn, vm);

				return;
			}
		}
	}

	public void threadStarted(VM vm, ThreadInfo startedThread)
	{
		if (startedThread.getId() > maxThreadID) maxThreadID = startedThread.getId();
				
		if (thModifiedID == startedThread.getId())
		{
			this.thModifiedEntryMethodSig = startedThread.getClassInfo().getName() + ".run()V";
		}
	}

	public int getMaxThreadID()
	{
		return this.maxThreadID;
	}

	public String getModifiedThreadEntryMethodSig()
	{
		return this.thModifiedEntryMethodSig;
	}

	public boolean beforeFirstModifiedCode()
	{
		return curTr.state == ExecState.BEFOREFIRST;
	}

	public boolean enteringModifiedCode()
	{
		return curTr.state == ExecState.ENTERING;
	}
	
	public boolean insideModifiedCode()
	{
		return curTr.state == ExecState.INSIDE;
	}
	
	public boolean exitingModifiedCode()
	{
		return curTr.state == ExecState.EXITING;
	}

	public boolean outsideModifiedCode()
	{
		return curTr.state == ExecState.OUTSIDE;
	}

	private void enableThreadChoicesForPossiblyConcurrentEvents(ThreadInfo curTh, Instruction curInsn, VM vm)
	{
		DynamicHappensBeforeOrdering listenerDynHBO = vm.getJPF().getListenerOfType(DynamicHappensBeforeOrdering.class);

		// we need to reverse the list of events because choices are ordered from the last to the first
		List<DynamicHappensBeforeOrdering.EventInfo> events = listenerDynHBO.getAllEvents();
		Collections.reverse(events);

		DynamicThreadChoice[] choices = vm.getChoiceGeneratorsOfType(DynamicThreadChoice.class);
		int curThChoicePos = 0;

		// loop over all relevant events that are not guaranteed to happen strictly before the beginning of the modified code segment
		for (DynamicHappensBeforeOrdering.EventInfo hboEv : events)
		{
			DynamicThreadChoice matchingDynChoice = null;

			// search for the matching dynamic thread choice for the current event based on code location
			
			while ((matchingDynChoice == null) && (curThChoicePos < choices.length))
			{
				DynamicThreadChoice curDynThChoice = choices[curThChoicePos];

				// we have the same thread
				if (hboEv.threadID == curDynThChoice.curThID)
				{
					// we have the same method
					if (hboEv.corrInsn.getMethodInfo().getFullName().equals(curDynThChoice.assocInsn.getMethodInfo().getFullName()))
					{
						// we have the same bytecode instruction
						if (hboEv.corrInsn.getPosition() == curDynThChoice.assocInsn.getPosition())
						{
							matchingDynChoice = curDynThChoice;
							break;
						}
					}
				}

				curThChoicePos++;
			}

			if (matchingDynChoice != null)
			{
				// for each relevant dynamic thread choice, we need to make sure there are at least two enabled threads and that one of them is the current thread that executes the matching event

				if (matchingDynChoice.isAvailableThread(hboEv.threadID))
				{
					matchingDynChoice.enableThread(hboEv.threadID);
				}

				// try other threads in this order: modified, other, runnable

				if ((matchingDynChoice.getTotalNumberOfEnabledThreads() < 2) && matchingDynChoice.isAvailableThread(thModifiedID))
				{
					matchingDynChoice.enableThread(thModifiedID);
				}

				if (matchingDynChoice.getTotalNumberOfEnabledThreads() < 2)
				{
					if (matchingDynChoice.isAvailableThread(thOtherID))
					{
						matchingDynChoice.enableThread(thOtherID);
					}
				}

				ThreadInfo[] availableThreads = matchingDynChoice.getAvailableThreads();
				int avThIdx = 0;

				while ( (matchingDynChoice.getTotalNumberOfEnabledThreads() < 2) && (avThIdx < availableThreads.length) )
				{
					matchingDynChoice.enableThread(availableThreads[avThIdx].getId());

					avThIdx++;
				}
			}
		}
	}


	// represents the state of the program counter of the modified thread with respect to boundaries of the modified code fragment (unit of change)
	static enum ExecState
	{
		BEFOREFIRST,
		ENTERING,
		INSIDE,
		EXITING,
		OUTSIDE
	};


	static class TransitionInfo
	{
		protected ExecState state;

		public TransitionInfo(ExecState st)
		{
			state = st;
		}
	}
}

