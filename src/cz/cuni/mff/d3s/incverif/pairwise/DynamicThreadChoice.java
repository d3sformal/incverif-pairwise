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

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.choice.ThreadChoiceFromSet;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;


/**
 * Special thread choice generator that supports dynamic adding of new choices on-the-fly.
 * All choices except the current thread are disabled initially (when the CG is created).
 */
public class DynamicThreadChoice extends ThreadChoiceFromSet
{
	// thread current when the choice is being created 
	public int curThID; 

	// IDs of all enabled threads (choices)
	private int[] enabledThreads;
	private int enabledThsCount;

	// IDs of all enabled but unexplored threads
	// we pick only from the beginning of the list
	private int[] unexploredThreads;
	private int unexploredThsCount;

	// current position in the list of unexplored threads
	// possible values: -1, 0
	private int unexploredThPos = -1;

	// instruction at which the choice generator was created
	public Instruction assocInsn;


	public DynamicThreadChoice(ThreadInfo[] thSet, ThreadInfo curTh, Instruction curPC)
	{
		super("dynamic", thSet, true);

		curThID = curTh.getId();

		enabledThreads = new int[thSet.length];
		enabledThsCount = 0;

		unexploredThreads = new int[thSet.length];
		unexploredThsCount = 0;

		assocInsn = curPC;
	}

	public void reset()
	{
		// the number of really explored choices
		count = 0;

		isDone = false;

		unexploredThsCount = 1;
		unexploredThreads[0] = curThID;

		for (int i = 0; i < enabledThsCount; i++)
		{
			if (enabledThreads[i] != curThID) 
			{
				unexploredThreads[unexploredThsCount] = enabledThreads[i];
				unexploredThsCount++;
			}
		}

		unexploredThPos = -1;
	}

	public ThreadInfo getNextChoice()
	{
		if (unexploredThPos < unexploredThsCount)
		{
			// we already know that a thread with the index equal to "unexploredThPos" is now enabled

			int nextThID = unexploredThreads[unexploredThPos];

			for (int i = 0; i < values.length; i++)
			{
				if (values[i].getId() == nextThID) return values[i];
			}
		}

		return null;
	}

	public boolean hasMoreChoices()
	{
		if (isDone) return false;

		if ((unexploredThPos == -1) || (unexploredThsCount > 1)) return true;

		return false;
	}

	public void advance()
	{
		if (unexploredThPos == -1)
		{
			unexploredThPos = 0;
		}
		else
		{
			// remove the previously explored choice
			for (int i = unexploredThPos; i < unexploredThsCount - 1; i++) unexploredThreads[i] = unexploredThreads[i+1];
			unexploredThsCount--;
		}

		if (count < enabledThsCount) count++; 
	}

	public int getTotalNumberOfChoices() 
	{
		return enabledThsCount;
	}

	public int getProcessedNumberOfChoices() 
	{
		return count;
	}

	public Object getNextChoiceObject() 
	{
		return getNextChoice();
	}

	public ThreadInfo[] getChoices()
	{
		ThreadInfo[] enabledThreadObjs = new ThreadInfo[enabledThsCount];
		int pos = 0;

		for (int i = 0; i < enabledThsCount; i++)
		{
			for (int j = 0; j < values.length; j++)
			{
				if (enabledThreads[i] == values[j].getId())
				{
					enabledThreadObjs[pos++] = values[j];
					break;
				}
			}
		}

		return enabledThreadObjs;
	}
	
	public boolean supportsReordering()
	{
		return false;
	}

	public void printOn(PrintWriter pw) 
	{
		pw.print(getClass().getName());
		pw.append("[id=\"");
		pw.append(id);
		pw.append('"');

		pw.append(",isCascaded:");
		pw.append(Boolean.toString(isCascaded));

		pw.print(",{");
		for (int i = 0; i < values.length; i++) 
		{
			if (i > 0) pw.print(',');
			pw.print(values[i].getName()+"("+values[i].getId()+")");
		}		
		pw.print("}");

		pw.print(",enabled:{");
		for (int i = 0; i < enabledThsCount; i++)
		{
			if (i > 0) pw.print(',');
			pw.print(enabledThreads[i]);
		}
		pw.print("}");

		pw.print(",unexplored:{");
		for (int i = 0; i < unexploredThsCount; i++)
		{
			if (i > 0) pw.print(',');
			if (i == unexploredThPos) pw.print(MARKER);			
			pw.print(unexploredThreads[i]);
		}
		pw.print("}]");
	}

	public ThreadInfo[] getAllThreadChoices() 
	{
		return getChoices();
	}

	@Override
	public boolean contains(ThreadInfo th)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i] == th)
			{
				for (int j = 0; j < enabledThsCount; j++)
				{
					if (enabledThreads[j] == th.getId()) return true;
				}
			}
		}
		
		return false;
	}

	public boolean allThreadsEnabled()
	{
		return (enabledThsCount == values.length);	
	}

	public int getTotalNumberOfEnabledThreads()
	{
		return enabledThsCount;
	}

	public void enableThread(int thId)
	{
		boolean isAlreadyEnabled = false;

		for (int i = 0; i < enabledThsCount; i++)
		{
			if (enabledThreads[i] == thId) isAlreadyEnabled = true;
		}

		boolean isAvailable = false;
	
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].getId() == thId) isAvailable = true;
		}

		// we do not want to enable some thread twice
		if ( ( ! isAlreadyEnabled ) && isAvailable )
		{
			enabledThreads[enabledThsCount++] = thId;
			unexploredThreads[unexploredThsCount++] = thId;
		}
	}

	public void enableAllThreads()
	{
		for (int i = 0; i < values.length; i++)
		{
			int thId = values[i].getId();

			enableThread(thId);
		}
	}

	public void enableThreads(ThreadInfo[] thObjSet)
	{
		for (int i = 0; i < thObjSet.length; i++)
		{
			int thId = thObjSet[i].getId();

			enableThread(thId);
		}
	}

	public boolean isAvailableThread(int thId)
	{
		for (int i = 0; i < values.length; i++)
		{
			if (values[i].getId() == thId) return true;
		}

		return false;
	}

	public ThreadInfo[] getAvailableThreads()
	{
		return values.clone();
	}

}

