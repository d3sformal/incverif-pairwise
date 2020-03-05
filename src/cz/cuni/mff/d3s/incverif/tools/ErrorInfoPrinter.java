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
import gov.nasa.jpf.Error;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.Transition;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.search.Search;


public class ErrorInfoPrinter extends ListenerAdapter 
{
	private String msgPrefix;


	public ErrorInfoPrinter(String prefix)
	{
		this.msgPrefix = prefix;
	}
	
	public void propertyViolated(Search search)
	{
		Error err = search.getCurrentError();
		
		Path pt = err.getPath();
		
		Transition tr = pt.getLast();
		
		ThreadInfo cti = tr.getThreadInfo();
	
		if (cti.getStackDepth() > 0)
		{
			for (StackFrame sf : cti)
			{
				if ( ! sf.isDirectCallFrame() )
				{
					System.out.println(msgPrefix + " : top stack frame trace info = " + sf.getStackTraceInfo());
					break;
				}
			}
		}
		else
		{
			// deadlock: current thread may have an empty call stack
			// instead, we make a string that contains the current PC of all stack frames for all other threads with non-empty call stack

			StringBuffer sb = new StringBuffer();

			for (ThreadInfo oti : cti.getVM().getThreadList())
			{
				if (oti.getStackDepth() > 0)
				{
					for (StackFrame sf : oti)
					{
						if ( ! sf.isDirectCallFrame() )
						{
							sb.append(sf.getStackTraceInfo());
						}
					}

					sb.append(" ; ");
				}
			}
	
			System.out.println(msgPrefix + " : threads with non-empty call stack = " + sb.toString());
		}
	}

}
