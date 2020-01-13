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


public class CodeBlockBoundary implements Cloneable
{
	public ProgramPoint startLoc;
	public ProgramPoint endLoc;

	private int hc;


	public CodeBlockBoundary(ProgramPoint sl, ProgramPoint el)
	{
		this.startLoc = sl;
		this.endLoc = el;

		hc = 0;
	}

	public boolean isValid()
	{
		return startLoc.methodSig.equals(endLoc.methodSig);
	}

	public String getMethodSignature()
	{
		// we assume that both locations (start, end) belong to the same method
		return startLoc.methodSig;
	}

	public boolean equals(Object obj)
	{
		if (obj == null) return false;

		if ( ! (obj instanceof CodeBlockBoundary) ) return false;

		CodeBlockBoundary other = (CodeBlockBoundary) obj;

		if ( ! this.startLoc.equals(other.startLoc) ) return false;
		if ( ! this.endLoc.equals(other.endLoc) ) return false;

		return true;
	}

	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + this.startLoc.hashCode();
			hc = hc * 31 + this.endLoc.hashCode();
		}

		return hc;
	}

	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();

		strbuf.append("<");
		strbuf.append(startLoc.toString());
		strbuf.append(":");
		strbuf.append(endLoc.toString());
		strbuf.append(">");
	
		return strbuf.toString();
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}

