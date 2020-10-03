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
import java.util.ArrayList;
import java.util.Stack;
import java.util.Collections;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;
import gov.nasa.jpf.jvm.bytecode.MONITORENTER;
import gov.nasa.jpf.jvm.bytecode.MONITOREXIT;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;


public class DynamicHappensBeforeOrdering extends ListenerAdapter
{
	// relevant actions performed in the current transition
	private static TransitionInfo curTr;

	// stack of transitions (current path/trace)
	private static Stack<TransitionInfo> curPathTrs;


	public DynamicHappensBeforeOrdering(Config cfg, JPF jpf)
	{
		curPathTrs = new Stack<TransitionInfo>();
	}


	public void executeInstruction(VM vm, ThreadInfo ti, Instruction insn)
	{
		if (ti.isFirstStepInsn())
		{
			curTr.threadId = ti.getId();
			return;
		}

		EventInfo ev = null;

		if (insn instanceof MONITORENTER)
		{
			MONITORENTER menInsn = (MONITORENTER) insn;
			
			int targetObjRef = ti.getTopFrame().peek();

			ev = new EventInfo(EventType.LOCK, ti.getId(), targetObjRef);
		}

		if (insn instanceof MONITOREXIT)
		{
			MONITOREXIT mexInsn = (MONITOREXIT) insn;
			
			int targetObjRef = ti.getTopFrame().peek();
			
			ev = new EventInfo(EventType.UNLOCK, ti.getId(), targetObjRef);
		}

		if (insn instanceof JVMInvokeInstruction)
		{
			JVMInvokeInstruction invokeInsn = (JVMInvokeInstruction) insn;

			MethodInfo tgtMethod = invokeInsn.getInvokedMethod();

			int targetObjRef = -1; 

			if (tgtMethod.isStatic())
			{
				targetObjRef = tgtMethod.getClassInfo().getClassObjectRef();
			}
			else
			{
				targetObjRef = ti.getCalleeThis(invokeInsn.getArgSize());
			}

			String tgtMthName = tgtMethod.getName();

			ClassInfo tgtMthCI = tgtMethod.getClassInfo();

			if (tgtMthName.equals("join") && isThreadClass(tgtMthCI))
			{
				ev = new EventInfo(EventType.TJOIN, ti.getId(), targetObjRef);
			}

			if (tgtMethod.isSynchronized())
			{
				ev = new EventInfo(EventType.LOCK, ti.getId(), targetObjRef);
			}
		}

		if (insn instanceof ReturnInstruction)
		{
			ReturnInstruction retInsn = (ReturnInstruction) insn;
		
			MethodInfo tgtMethod = retInsn.getMethodInfo();
		
			if (tgtMethod.isSynchronized())
			{
				int targetObjRef = -1;
				
				if (tgtMethod.isStatic())
				{
					targetObjRef = tgtMethod.getClassInfo().getClassObjectRef();	
				}
				else
				{
					targetObjRef = ti.getThis();
				}

				ev = new EventInfo(EventType.UNLOCK, ti.getId(), targetObjRef);
			}
		}

		if (ev != null)
		{
			curTr.events.add(ev);
		}
	}

	private static boolean isThreadClass(ClassInfo ci)
	{
		if (ci.isThreadClassInfo()) return true;

		ClassInfo ciSuper = ci.getSuperClass();
		while (ciSuper != null)
		{
			if (ciSuper.isThreadClassInfo()) return true;
			ciSuper = ciSuper.getSuperClass();
		}

		return false;
	} 

	public void searchStarted(Search search) 
	{
		curTr = new TransitionInfo();
	}
	
	public void stateAdvanced(Search search) 
	{
		curPathTrs.push(curTr);
		
		curTr = new TransitionInfo();
	}
	
	public void stateBacktracked(Search search) 
	{
		curPathTrs.pop();
	}


	static enum EventType
	{
		LOCK,
		UNLOCK,
		TJOIN
	}

	static class EventInfo
	{
		public EventType type;

		public int threadID;

		// lock object for acquire/release
		// thread object for join
		public int tgtObjectID;
	
		public EventInfo(EventType et, int thID, int toID)
		{
			type = et;
			threadID = thID;
			tgtObjectID = toID;
		}
	}
	
	
	static class TransitionInfo
	{
		// thread which executed this transition
		public int threadId;
		
		// visible actions
		public List<EventInfo> events;
		
		
		public TransitionInfo()
		{
			events = new ArrayList<EventInfo>();			
		}
	}
}

