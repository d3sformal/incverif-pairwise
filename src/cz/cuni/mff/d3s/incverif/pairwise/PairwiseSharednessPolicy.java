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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ApplicationContext;
import gov.nasa.jpf.vm.PathSharednessPolicy;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;


public class PairwiseSharednessPolicy extends PathSharednessPolicy  
{
	// relevant thread IDs
		// thread with modified code (T)
		// selected other thread (T_o, T2)
	private int thModifiedID;
	private int thOtherID;


	public PairwiseSharednessPolicy(Config config)
	{
		super(config);

		thModifiedID = config.getInt("incverif.pairwise.thread.modified.id", -1);
		thOtherID = config.getInt("incverif.pairwise.thread.other.id", -1);
	}

	public void initializeSharednessPolicy(VM vm, ApplicationContext appCtx)
	{
		super.initializeSharednessPolicy(vm, appCtx);	
	}

	protected ThreadInfo[] getRunnables(ApplicationContext appCtx)
	{
		ThreadInfo[] originalThArray = super.getRunnables(appCtx);

		ThreadExecutionMonitor listenerThExecMon = vm.getJPF().getListenerOfType(ThreadExecutionMonitor.class);

		ThreadInfo[] filteredThArray = SchedulingHelper.determinePairwiseEnabledThreads(originalThArray, thModifiedID, thOtherID, listenerThExecMon);

		//System.out.println("[DEBUG PP] SharednessPolicy: originalThArray = " + java.util.Arrays.toString(originalThArray) + ", thModifiedID = " + thModifiedID + ", thOtherID = " + thOtherID + ", filteredThArray = " + java.util.Arrays.toString(filteredThArray));

		return filteredThArray;
	}
}

