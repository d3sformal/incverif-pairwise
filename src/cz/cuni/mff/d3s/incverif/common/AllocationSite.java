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


public class AllocationSite extends ObjectID implements Comparable<AllocationSite>
{
	public ProgramPoint progPoint;

	private int hc;
	
	
	public AllocationSite(String mthSig, int insnIndex, int insnBcPos, int insnBcIndex)
	{
		this(new ProgramPoint(mthSig, insnIndex, insnBcPos, insnBcIndex));
	}
	
	private AllocationSite(ProgramPoint pp)
	{
		super();
		
		this.progPoint = pp;
		
		hc = 0;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		
		if ( ! (obj instanceof AllocationSite) ) return false;
		
		AllocationSite other = (AllocationSite) obj;
		
		if ( ! this.progPoint.equals(other.progPoint) ) return false;
		
		return true;
	}
	
	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + this.progPoint.hashCode();
		}
		
		return hc;
	}
	
	public int compareTo(AllocationSite other)
	{
		if (other == null) return 1;
		
		return this.progPoint.compareTo(other.progPoint);
	}

	protected String createStringRepr()
	{
		return this.progPoint.toString();
	}
}
