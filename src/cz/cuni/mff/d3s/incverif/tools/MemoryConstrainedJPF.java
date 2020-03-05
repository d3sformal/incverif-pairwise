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

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.search.Search;


public class MemoryConstrainedJPF extends ListenerAdapter 
{
	private long maxMemoryLimitMB = 0;
	private long freeMemoryLimitMB = 0;

	private boolean limitReached = false;


	public MemoryConstrainedJPF(Config cfg, JPF jpf)
	{
	}
	
	public void searchStarted(Search search)
	{
		VM vm = search.getVM();
		Config config = search.getConfig();

		this.maxMemoryLimitMB = config.getInt("jpf.max_memory_limit", 8192);
		this.freeMemoryLimitMB = config.getInt("jpf.free_memory_limit", 512);
	}
	
	public void stateAdvanced(Search search) 
	{
		long curTotalMemoryMB = Runtime.getRuntime().totalMemory() >> 20; // convert to MB
		long curFreeMemoryMB = Runtime.getRuntime().freeMemory() >> 20; // convert to MB

		if ( (curFreeMemoryMB < freeMemoryLimitMB) && (curTotalMemoryMB > (maxMemoryLimitMB - freeMemoryLimitMB)) )
		{
			System.out.println("[LOG] terminating search because memory limit was reached");

			limitReached = true;

			search.terminate();
		}
	}

	public boolean isLimitReached()
	{
		return limitReached;
	}
}
