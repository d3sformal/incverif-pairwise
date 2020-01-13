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


public class SynchEventID
{
	// target object
	public AllocationSite tgtObjectID;
	
	public SynchEventType eventType;
	
	private int hc;


	public SynchEventID(AllocationSite objID, SynchEventType evType)
	{
		this.tgtObjectID = objID;
		
		this.eventType = evType;
		
		hc = 0;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		
		if ( ! (obj instanceof SynchEventID) ) return false;
		
		SynchEventID other = (SynchEventID) obj;
		
		if ( ! this.tgtObjectID.equals(other.tgtObjectID) ) return false;
		if (this.eventType != other.eventType) return false;
		
		return true;
	}
	
	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + this.tgtObjectID.hashCode();
			hc = hc * 31 + this.eventType.hashCode();
		}
		
		return hc;
	}
	
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();

		strbuf.append(tgtObjectID.toString());
		strbuf.append(":");
		strbuf.append(eventType.toString());

		return strbuf.toString();
	}
}
