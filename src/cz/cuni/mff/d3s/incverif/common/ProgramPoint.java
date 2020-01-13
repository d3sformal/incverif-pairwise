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


public class ProgramPoint implements Comparable<ProgramPoint>
{
	public String methodSig;
	
	// index to the list of instructions within some internal representation (e.g., Shrike BT)
	public int insnIndex;

	// instruction position in real bytecode
	public int insnBcPos;

	// index within real/actual bytecode
	public int insnBcIndex;

	private int hc;


	public ProgramPoint(String mthSig, int insnIndex, int insnBcPos, int insnBcIndex)
	{
		this.methodSig = mthSig;

		this.insnIndex = insnIndex;
		
		this.insnBcPos = insnBcPos;
		this.insnBcIndex = insnBcIndex;

		hc = 0;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		
		if ( ! (obj instanceof ProgramPoint) ) return false;
		
		ProgramPoint other = (ProgramPoint) obj;
		
		if ( ! this.methodSig.equals(other.methodSig) ) return false;

		if (this.insnIndex != other.insnIndex) return false;
		
		if (this.insnBcPos != other.insnBcPos) return false;
		if (this.insnBcIndex != other.insnBcIndex) return false;
	
		return true;
	}
	
	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + this.methodSig.hashCode();

			hc = hc * 31 + this.insnIndex;

			hc = hc * 31 + this.insnBcPos;
			hc = hc * 31 + this.insnBcIndex;
		}
		
		return hc;
	}
	
	public int compareTo(ProgramPoint other)
	{
		if (other == null) return 1;
		
		int cmpRes = 0;
		
		cmpRes = this.methodSig.compareTo(other.methodSig);
		if (cmpRes != 0) return cmpRes;

		if (this.insnIndex < other.insnIndex) return -1;
		else if (this.insnIndex > other.insnIndex) return 1;
		
		if (this.insnBcPos < other.insnBcPos) return -1;
		else if (this.insnBcPos > other.insnBcPos) return 1;
		
		if (this.insnBcIndex < other.insnBcIndex) return -1;
		else if (this.insnBcIndex > other.insnBcIndex) return 1;
	
		return 0;
	}
	
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();

		strbuf.append(methodSig);
		strbuf.append(":");
		strbuf.append("[");
		strbuf.append("index="+insnIndex);
		strbuf.append(",");
		strbuf.append("bcidx="+insnBcIndex);
		strbuf.append(",");
		strbuf.append("bcpos="+insnBcPos);
		strbuf.append("]");
		
		return strbuf.toString();
	}

}
