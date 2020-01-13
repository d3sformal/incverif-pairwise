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

import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadList;


public class SchedulingHelper
{
	public static ThreadInfo[] determinePairwiseEnabledThreads(ThreadInfo[] allRunnableThreads, int thModifiedID, int thOtherID, ThreadExecutionMonitor listenerThExecMon)
	{
		// goal: determine the set of enabled runnable threads at the current scheduling point

		int thCurrentID = ThreadInfo.getCurrentThread().getId();
	
		ThreadList vmThreadsList = ThreadInfo.getCurrentThread().getVM().getThreadList();

		// may be null if a given thread does not exist yet
		ThreadInfo thModified = vmThreadsList.getThreadInfoForId(thModifiedID);
		ThreadInfo thOther = vmThreadsList.getThreadInfoForId(thOtherID);
		ThreadInfo thCurrent = vmThreadsList.getThreadInfoForId(thCurrentID);

		if (listenerThExecMon.beforeFirstModifiedCode())
		{
			// if thread T (modified) is the current one at the scheduling point and it is still runnable then choose it
			if ( ( (thModified != null ) && (thModified.getId() == thCurrentID) ) && (thCurrent.isRunnable() || thCurrent.isTimeoutRunnable()) )
			{
				return new ThreadInfo[] { thModified };
			}

			// if a thread T (which contains the modified code) is enabled then schedule T from the given state
			if ( (thModified != null) && (thModified.isRunnable() || thModified.isTimeoutRunnable()) ) 
			{
				return new ThreadInfo[] { thModified };
			}

			// else if the current thread is runnable at the scheduling point then choose that one
			if (thCurrent.isRunnable() || thCurrent.isTimeoutRunnable())
			{
				return new ThreadInfo[] { thCurrent };
			}

			// otherwise pick an arbitrary other thread from the set of all runnable threads (e.g., the one with the minimal ID)
			// the list of all runnable threads may be empty when all of threads are terminated or blocked
			if (allRunnableThreads.length > 0)
			{
				return new ThreadInfo[] { allRunnableThreads[0] };
			}
		}

		if (listenerThExecMon.enteringModifiedCode() || listenerThExecMon.insideModifiedCode())
		{
			// the search process either (1) just reached the interfering action that lies at the beginning of the modified code fragment in thread T, or (2) is currently executing the modified code fragment in T

			// if both the modified thread (T) the other thread (T2) are runnable in the current state then the search process can start exploring the interleavings of T (modified code) and T2 right away
			if ( ( (thModified != null) && (thModified.isRunnable() || thModified.isTimeoutRunnable()) ) && ( (thOther != null) && (thOther.isRunnable() || thOther.isTimeoutRunnable()) ) )
			{
				return new ThreadInfo[] { thModified, thOther };
			}
		
			// else if just thread T is runnable in the current state then it is scheduled (it is not runnable only when it just executed a lock acquire instruction and got blocked there)
			if ( (thModified != null) && (thModified.isRunnable() || thModified.isTimeoutRunnable()) )
			{
				return new ThreadInfo[] { thModified };
			}

			// otherwise, one arbitrary thread is scheduled (until the modified thread T is enabled at some dynamic state)
			// the list of all runnable threads may be empty when all of threads are terminated or blocked
			if (allRunnableThreads.length > 0)
			{
				return new ThreadInfo[] { allRunnableThreads[0] };
			}
		}

		if (listenerThExecMon.exitingModifiedCode() || listenerThExecMon.outsideModifiedCode())
		{
			// the search process left the modified code fragment in thread T on the current execution trace (interleaving)
			// however, execution may reach the modified code again sometime later after exiting

			// we disable all thread scheduling choices by always picking just a single arbitrary runnable thread (e.g., the one with the minimal ID)
				// heuristic: we give a preference to the current thread (when it is runnable) or the modified thread T

			if (thCurrent.isRunnable() || thCurrent.isTimeoutRunnable())
			{
				return new ThreadInfo[] { thCurrent };
			}

			if ( (thModified != null) && (thModified.isRunnable() || thModified.isTimeoutRunnable()) )
			{
				return new ThreadInfo[] { thModified };
			}

			// otherwise pick an arbitrary other thread from the set of all runnable threads (e.g., the one with the minimal ID)
			// the list of all runnable threads may be empty when all of threads are terminated or blocked
			if (allRunnableThreads.length > 0)
			{
				return new ThreadInfo[] { allRunnableThreads[0] };
			}
		}

		// default
		// array could be empty
		return allRunnableThreads;
	}
}

