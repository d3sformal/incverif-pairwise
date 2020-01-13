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


public class ArrayID
{
	// abstract heap object that represents this array
	public ObjectID heapObjectID;
	
	public int dimensions;
	

	public ArrayID(ObjectID objID, int dim)
	{
		this.heapObjectID = objID;
		this.dimensions = dim;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == null) return false;
		
		if ( ! (obj instanceof ArrayID) ) return false;
		
		ArrayID other = (ArrayID) obj;
		
		if ( ! this.heapObjectID.equals(other.heapObjectID) ) return false;
		if (this.dimensions != other.dimensions) return false;
		
		return true;
	}
	
	public int hashCode()
	{
		return this.heapObjectID.hashCode() + this.dimensions;
	}
	
	public String toString()
	{
		StringBuffer strbuf = new StringBuffer();
		
		strbuf.append(this.heapObjectID.toString());
		
		for (int i = 0; i < this.dimensions; i++) strbuf.append("[]");
		
		return strbuf.toString();
	}	
}

