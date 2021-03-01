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
import java.util.Iterator;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;
import gov.nasa.jpf.jvm.bytecode.MONITORENTER;
import gov.nasa.jpf.jvm.bytecode.MONITOREXIT;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;

import cz.cuni.mff.d3s.incverif.common.Utils;


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


	public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction execInsn)
	{
		if (ti.isFirstStepInsn())
		{
			curTr.threadId = ti.getId();
			return;
		}

		// we can ignore all events associated with Java standard library methods
		if (Utils.isJavaStandardLibraryMethod(execInsn.getMethodInfo().getFullName())) return;

		EventInfo ev = null;

		if (execInsn instanceof MONITORENTER)
		{
			MONITORENTER menInsn = (MONITORENTER) execInsn;
			
			int targetObjRef = menInsn.getLastLockRef();

			ev = new EventInfo(EventType.LOCK, ti.getId(), targetObjRef, execInsn);

			dropAllEventsForThread(ti.getId());

			dropAllEventsPrecedingLockReleaseForOtherThreads(ti.getId(), targetObjRef, vm);
		}

		if (execInsn instanceof MONITOREXIT)
		{
			MONITOREXIT mexInsn = (MONITOREXIT) execInsn;
			
			int targetObjRef = mexInsn.getLastLockRef();
			
			ev = new EventInfo(EventType.UNLOCK, ti.getId(), targetObjRef, execInsn);
		}

		if (execInsn instanceof JVMInvokeInstruction)
		{
			JVMInvokeInstruction invokeInsn = (JVMInvokeInstruction) execInsn;

			MethodInfo tgtMethod = null;

			String tgtMethodSig = invokeInsn.getInvokedMethodClassName() + "." + invokeInsn.getInvokedMethodName();

			String tgtMethodNameDesc = tgtMethodSig.substring(tgtMethodSig.lastIndexOf('.') + 1);

			if (execInsn.getMethodInfo().getClassInfo() != null)
			{
				ClassInfo tgtMthDeclOwnerClass = execInsn.getMethodInfo().getClassInfo().resolveReferencedClass(invokeInsn.getInvokedMethodClassName());

				// getMethod returns "null" if the desired method is actually declared in a super-interface
				tgtMethod = tgtMthDeclOwnerClass.getMethod(tgtMethodNameDesc, true);
			}

			int targetObjRef = -1; 

			if ((tgtMethod != null) && tgtMethod.isStatic())
			{
				targetObjRef = tgtMethod.getClassInfo().getClassObjectRef();
			}
			else
			{
				targetObjRef = invokeInsn.getLastObjRef();

				if ((targetObjRef != -1) && (tgtMethod == null))
				{
					ElementInfo tgtMthDynOwnerObj = vm.getHeap().get(targetObjRef);

					if (tgtMthDynOwnerObj != null)
					{
						ClassInfo tgtMthDynOwnerClass = tgtMthDynOwnerObj.getClassInfo();

						tgtMethod = tgtMthDynOwnerClass.getMethod(tgtMethodNameDesc, true);
					}
				}
			}

			if (tgtMethod != null)
			{
				String tgtMthName = tgtMethod.getName();

				ClassInfo tgtMthCI = tgtMethod.getClassInfo();

				if (tgtMthName.equals("join") && isThreadClass(tgtMthCI))
				{
					ev = new EventInfo(EventType.TJOIN, ti.getId(), targetObjRef, execInsn);
			
					int targetThID = vm.getThreadList().getThreadInfoForObjRef(targetObjRef).getId(); 

					dropAllEventsForThread(targetThID);
				}

				if (tgtMethod.isSynchronized())
				{
					ev = new EventInfo(EventType.LOCK, ti.getId(), targetObjRef, execInsn);

					dropAllEventsForThread(ti.getId());

					dropAllEventsPrecedingLockReleaseForOtherThreads(ti.getId(), targetObjRef, vm);
				}
			}
		}

		if (execInsn instanceof ReturnInstruction)
		{
			ReturnInstruction retInsn = (ReturnInstruction) execInsn;
		
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

				ev = new EventInfo(EventType.UNLOCK, ti.getId(), targetObjRef, execInsn);
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

	private void dropAllEventsForThread(int tgtThID)
	{
		// remove all events associated with the given thread ID

		Iterator<TransitionInfo> cpTrIt = curPathTrs.iterator();
		while (cpTrIt.hasNext())
		{
			TransitionInfo tr = cpTrIt.next();

			Iterator<EventInfo> trEvIt = tr.events.iterator();
			while (trEvIt.hasNext())
			{
				EventInfo ev = trEvIt.next();

				if (ev.threadID == tgtThID) trEvIt.remove();
			}
		}
	
		Iterator<EventInfo> ctEvIt = curTr.events.iterator();
		while (ctEvIt.hasNext())
		{
			EventInfo ev = ctEvIt.next();

			if (ev.threadID == tgtThID) ctEvIt.remove();
		}
	}

	private void dropAllEventsPrecedingLockReleaseForOtherThreads(int tgtThID, int lockObjRef, VM vm)
	{
		// for each thread T other than the input thread (ID), find the most recent "lock release" event by thread T on the same lock object, and then drop the "lock release" event and all events by the same thread T that precede it

		for (ThreadInfo curTh : vm.getThreadList())
		{
			// skip the given thread
			if (curTh.getId() == tgtThID) continue;

			boolean foundLockRelease = false;

			foundLockRelease = findLockReleaseAndRemovePrecedingEventsByThread(curTr, curTh.getId(), lockObjRef);

			Iterator<TransitionInfo> cpTrIt = curPathTrs.iterator();
			while (cpTrIt.hasNext())
			{
				TransitionInfo tr = cpTrIt.next();

				if (foundLockRelease)
				{
					// we can safely drop all events for a given thread in this transition, because lock release was found in a subsequent transition

					Iterator<EventInfo> trEvIt = tr.events.iterator();
					while (trEvIt.hasNext())
					{
						EventInfo ev = trEvIt.next();

						if (ev.threadID == curTh.getId()) trEvIt.remove();
					}
				}
				else
				{
					foundLockRelease = findLockReleaseAndRemovePrecedingEventsByThread(tr, curTh.getId(), lockObjRef);
				}
			}
		}
	}

	private boolean findLockReleaseAndRemovePrecedingEventsByThread(TransitionInfo inputTr, int tgtThID, int lockObjRef)
	{
		boolean found = false;

		for (int i = inputTr.events.size() - 1; i >= 0; i--)
		{
			EventInfo trEv = inputTr.events.get(i);

			if (trEv.type == EventType.UNLOCK)
			{
				if ( (trEv.threadID == tgtThID) && (trEv.tgtObjectID == lockObjRef) )
				{
					found = true;

					// loop just over preceding events in the given transition
					Iterator<EventInfo> trPrecEvIt = inputTr.events.iterator();
					while (trPrecEvIt.hasNext())
					{
						EventInfo precEv = trPrecEvIt.next();

						if (precEv.threadID == tgtThID) trPrecEvIt.remove();
	
						// we reached the lock release action
						if (precEv == trEv) break;
					}

					break;
				}
			}
		}

		return found;
	}

	public List<EventInfo> getAllEvents()
	{
		List<EventInfo> allEvents = new ArrayList<EventInfo>();

		for (TransitionInfo tr : curPathTrs)
		{
			allEvents.addAll(tr.events);
		}

		allEvents.addAll(curTr.events);

		return allEvents;
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

		// corresponding Java bytecode instruction
		// needed for data about code location
		public Instruction corrInsn;
	
		public EventInfo(EventType et, int thID, int toID, Instruction ci)
		{
			type = et;
			threadID = thID;
			tgtObjectID = toID;
			corrInsn = ci;
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

