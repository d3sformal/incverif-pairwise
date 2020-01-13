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
import gov.nasa.jpf.search.Search;


public class TimeConstrainedJPF extends ListenerAdapter 
{
	private long maxTime = 0;
	private long startTime = 0;

	public TimeConstrainedJPF(Config cfg, JPF jpf)
	{
	}
	
	public void searchStarted(Search search)
	{
		VM vm = search.getVM();
		Config config = search.getConfig();

		this.startTime = System.currentTimeMillis();
		
		this.maxTime = config.getInt("jpf.time_limit", -1) * 1000; // convert to milliseconds
	}
	
	public void stateAdvanced(Search search) 
	{
		long duration = System.currentTimeMillis() - this.startTime;

		if (duration >= maxTime)
		{
			System.out.println("[LOG] terminating search because time limit was reached");

			duration = duration / 1000;
			search.terminate();
		}
	}
}
