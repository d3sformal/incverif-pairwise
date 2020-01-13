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


public class LocalVarID extends ObjectID
{
	public String methodSig;
	
	// SSA value number
	public int varNumber;

	public String typeName;
	
	private int hc;

	
	public LocalVarID(String mthSig, int varNo)
	{
		this(mthSig, varNo, null);
	}
	
	public LocalVarID(String mthSig, int varNo, String typeNm)
	{
		super();
		
		this.methodSig = mthSig;
		this.varNumber = varNo;
		
		this.typeName = typeNm;
		
		hc = 0;
	}

	
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		
		if ( ! (obj instanceof LocalVarID) ) return false;
		
		LocalVarID other = (LocalVarID) obj;
		
		if ( ! this.methodSig.equals(other.methodSig) ) return false;
		if (this.varNumber != other.varNumber) return false;
		
		// we do not consider type name here because it may not be set
		
		return true;
	}
	
	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + 1 + this.methodSig.hashCode();
			hc = hc * 31 + this.varNumber;
		}

		// we do not consider type name here because it may not be set
		
		return hc;
	}
	
	protected String createStringRepr()
	{
		StringBuffer strbuf = new StringBuffer();

		strbuf.append(methodSig);
		strbuf.append(":");
		strbuf.append(varNumber);
		
		if (typeName != null)
		{
			strbuf.append(":");
			strbuf.append(typeName);
		}
		
		return strbuf.toString();
	}
}
