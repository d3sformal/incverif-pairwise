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


public class FieldID
{
	// target object (class name for static fields, allocation site for instance fields)
	public ObjectID tgtObjectID;
	
	public String fieldName;
	
	private int hc;


	public FieldID(ObjectID objID, String fName)
	{
		this.tgtObjectID = objID;
		
		this.fieldName = fName;

		hc = 0;
	}

	public boolean isStatic()
	{
		if (tgtObjectID == null) return false;

		if (tgtObjectID instanceof ClassName) return true;

		// tgtObjectID instanceof AllocationSite
		return false;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		
		if ( ! (obj instanceof FieldID) ) return false;
		
		FieldID other = (FieldID) obj;
		
		if ( ! this.tgtObjectID.equals(other.tgtObjectID) ) return false;
		if ( ! this.fieldName.equals(other.fieldName) ) return false;
		
		return true;
	}
	
	public int hashCode()
	{
		if (hc == 0)
		{
			hc = hc * 31 + this.tgtObjectID.hashCode();
			hc = hc * 31 + this.fieldName.hashCode();
		}
		
		return hc;
	}
	
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();

		strbuf.append(tgtObjectID.toString());
		strbuf.append(".");
		strbuf.append(fieldName);

		return strbuf.toString();
	}	
}

