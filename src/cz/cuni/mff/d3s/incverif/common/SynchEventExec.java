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
package cz.cuni.mff.d3s.incverif.common;


public class SynchEventExec
{
	public SynchEventID targetEvent;

	public ProgramPoint progPoint;

	private int hc;


	public SynchEventExec(SynchEventID tgtEvent, ProgramPoint pp)
	{
		this.targetEvent = tgtEvent;

		this.progPoint = pp;

		hc = 0;
	}

	public boolean equals(Object obj)
	{
		if (obj == null) return false;

		if ( ! (obj instanceof SynchEventExec) ) return false;

		SynchEventExec other = (SynchEventExec) obj;

		if ( ! this.targetEvent.equals(other.targetEvent) ) return false;
		if ( ! this.progPoint.equals(other.progPoint) ) return false;

		return true;
	}

	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + this.targetEvent.hashCode();
			hc = hc * 31 + this.progPoint.hashCode();
		}

		return hc;
	}

	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();

		strbuf.append(targetEvent.toString());
		strbuf.append(" # ");
		strbuf.append(progPoint.toString());

		return strbuf.toString();
	}
}

