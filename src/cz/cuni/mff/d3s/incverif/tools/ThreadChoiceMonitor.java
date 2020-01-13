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
package cz.cuni.mff.d3s.incverif.tools;

import java.util.Arrays;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;


public class ThreadChoiceMonitor extends ListenerAdapter 
{
	public ThreadChoiceMonitor(Config config, JPF jpf) 
	{
	}

	public void choiceGeneratorRegistered(VM vm, ChoiceGenerator<?> nextCG, ThreadInfo curTh, Instruction insn) 
	{
		if ( ! (nextCG instanceof ThreadChoiceFromSet) ) return;

		if (insn == null) return;

		MethodInfo mi = insn.getMethodInfo();
		if (mi == null) return;

		ClassInfo ci = mi.getClassInfo();
		if (ci == null) return;

		System.out.println("[CHOICE] current thread ID = " + curTh.getId() + ", method signature = " + ci.getName() + "." + mi.getUniqueName() + ", bytecode position = " + insn.getPosition() + ", runnable threads = " + Arrays.toString(getRunnableThreadsIDs(vm)));
	}

	private static int[] getRunnableThreadsIDs(VM vm)
	{
		ThreadInfo[] threadObjs = vm.getThreadList().getProcessTimeoutRunnables(vm.getCurrentApplicationContext());

		int[] threadIDs = new int[threadObjs.length];

		for (int i = 0; i < threadObjs.length; i++)
		{
			threadIDs[i] = threadObjs[i].getId();
		}

		return threadIDs;
	}

}
 

