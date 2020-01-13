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

import java.util.Stack;

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

	// current transition
	// includes the current execution state of thread with modified code
	private TransitionInfo curTr;

	// stack of transitions on the current execution trace
	private Stack<TransitionInfo> curTraceTrs;

	// the maximum thread ID observed during the run of JPF
	private int maxThreadID;


	public ThreadExecutionMonitor(Config cfg, int tmid, CodeBlockBoundary mcbb)
	{
		this.config = cfg;

		this.thModifiedID = tmid;
		this.modifiedCodeBoundary = mcbb;

		this.curTraceTrs = new Stack<TransitionInfo>();

		this.maxThreadID = 0;
	}

	public void searchStarted(Search search)
	{
		// we need to create and use a special root transition so that backtracking works properly
		TransitionInfo rootTr = new TransitionInfo(ExecState.BEFOREFIRST);
		curTraceTrs.push(rootTr);

		curTr = new TransitionInfo(ExecState.BEFOREFIRST);

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

	public void executeInstruction(VM vm, ThreadInfo curTh, Instruction insn)
	{
		if (curTh.isFirstStepInsn()) return;

		// we care just about the modified thread
		if (curTh.getId() != thModifiedID) return;

		Instruction curThPC = curTh.getPC();

		if ( ! modifiedCodeBoundary.getMethodSignature().equals(curThPC.getMethodInfo().getFullName()) ) return; 

		int thModifiedCurInsnBcPos = curThPC.getPosition();

		if (curTr.state == ExecState.BEFOREFIRST)
		{
			if (thModifiedCurInsnBcPos == modifiedCodeBoundary.startLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): before first (" + ExecState.BEFOREFIRST.ordinal() + ") -> entering (" + ExecState.ENTERING.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.ENTERING;

				return;
			}
			
			// this may happen in the case of jumps directly into the bytecode range
			if ( (thModifiedCurInsnBcPos > modifiedCodeBoundary.startLoc.insnBcPos) && (thModifiedCurInsnBcPos < modifiedCodeBoundary.endLoc.insnBcPos) )
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): before first (" + ExecState.BEFOREFIRST.ordinal() + ") -> inside (" + ExecState.INSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.INSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.ENTERING)
		{
			if (thModifiedCurInsnBcPos != modifiedCodeBoundary.startLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): entering (" + ExecState.ENTERING.ordinal() + ") -> inside (" + ExecState.INSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.INSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.INSIDE)
		{
			if (thModifiedCurInsnBcPos == modifiedCodeBoundary.endLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): inside (" + ExecState.INSIDE.ordinal() + ") -> exiting (" + ExecState.EXITING.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.EXITING;

				return;
			}

			// this may happen in the case of jumps directly out of the bytecode range
			if ( (thModifiedCurInsnBcPos > modifiedCodeBoundary.endLoc.insnBcPos) || (thModifiedCurInsnBcPos < modifiedCodeBoundary.startLoc.insnBcPos) )
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): inside (" + ExecState.INSIDE.ordinal() + ") -> outside (" + ExecState.OUTSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.OUTSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.EXITING)
		{
			if (thModifiedCurInsnBcPos != modifiedCodeBoundary.endLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): exiting (" + ExecState.EXITING.ordinal() + ") -> outside (" + ExecState.OUTSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.OUTSIDE;

				return;
			}
		}

		if (curTr.state == ExecState.OUTSIDE)
		{
			if (thModifiedCurInsnBcPos == modifiedCodeBoundary.startLoc.insnBcPos)
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): outside (" + ExecState.OUTSIDE.ordinal() + ") -> entering (" + ExecState.ENTERING.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.ENTERING;

				return;
			}

			// this may happen in the case of jumps directly into the bytecode range
			if ( (thModifiedCurInsnBcPos > modifiedCodeBoundary.startLoc.insnBcPos) && (thModifiedCurInsnBcPos < modifiedCodeBoundary.endLoc.insnBcPos) )
			{
				//System.out.println("[DEBUG PP] ThreadExecutionMonitor (forward insn): outside (" + ExecState.OUTSIDE.ordinal() + ") -> inside (" + ExecState.INSIDE.ordinal() + "), current thread ID = " + curTh.getId() + ", current PC = " + curThPC.getMethodInfo().getFullName() + ":[bcpos=" + thModifiedCurInsnBcPos + "]");

				curTr.state = ExecState.INSIDE;

				return;
			}
		}
	}

	public void threadStarted(VM vm, ThreadInfo startedThread)
	{
		if (startedThread.getId() > maxThreadID) maxThreadID = startedThread.getId();
	}

	public int getMaxThreadID()
	{
		return this.maxThreadID;
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

