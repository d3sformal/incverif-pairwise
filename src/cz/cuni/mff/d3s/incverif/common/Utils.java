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

import java.util.Set;


public class Utils
{
	// the identifier can be either (1) a full method signature or (2) a fully qualified field name in the format "<class name>.<plain field name>"
	public static String extractClassName(String identifier)
	{
		int k = identifier.lastIndexOf('.');
		String className = identifier.substring(0, k);
		return className;
	}

	public static String extractPlainMethodName(String methodSig)
	{
		int k1 = methodSig.lastIndexOf('.');
		int k2 = methodSig.indexOf('(');
		String methodName = methodSig.substring(k1+1,k2);
		return methodName;
	}

	public static String extractMethodDescriptor(String methodSig)
	{
		int k = methodSig.indexOf('(');
		String mthDesc = methodSig.substring(k);
		return mthDesc;
	}

	public static String getPlainTypeName(String internalTypeName)
	{
		int curPos = 0;
		
		// count array dimensions
		int arrayDims = 0;
		while (internalTypeName.charAt(curPos) == '[')
		{
			curPos++;
			arrayDims++;
		}
		
		StringBuffer plainTypeNameSB = new StringBuffer();
		
		switch (internalTypeName.charAt(curPos))
		{
			case 'Z':
				plainTypeNameSB.append("boolean");
				break;
			case 'B':
				plainTypeNameSB.append("byte");
				break;
			case 'C':
				plainTypeNameSB.append("char");
				break;
			case 'I':
				plainTypeNameSB.append("int");
				break;
			case 'S':
				plainTypeNameSB.append("short");
				break;
			case 'J':
				plainTypeNameSB.append("long");
				break;
			case 'F':
				plainTypeNameSB.append("float");
				break;
			case 'D':
				plainTypeNameSB.append("double");
				break;
			case 'V':
				plainTypeNameSB.append("void");
				break;
			case 'L': // references		
				plainTypeNameSB.append(getPlainClassName(internalTypeName.substring(curPos)));
				break;
			default:
		}
		
		for (int i = 0; i < arrayDims; i++)
		{
			plainTypeNameSB.append("[]");
		}
		
		return plainTypeNameSB.toString();
	}

	public static String getPlainClassName(String internalClassName)
	{
		int endPos = internalClassName.length();
		
		// omit the ";" character at the end if present
		if (internalClassName.charAt(endPos - 1) == ';') endPos--;
		
		// skip the character "L" at the beginning
		return internalClassName.substring(1, endPos).replace('/', '.');
	}

	public static String getInternalClassName(String plainClassName)
	{
		return getInternalClassName(plainClassName, true);
	}
	
	public static String getInternalClassName(String plainClassName, boolean withSemicolon)
	{
		return "L" + plainClassName.replace('.', '/') + (withSemicolon ? ";" : "");
	}

	public static boolean isThreadLocalObject(ObjectID objID, Set<AllocationSite> sharedObjAllocSites)
	{
		// static data are shared by definition
		if (objID instanceof ClassName) return false;

		if (objID instanceof AllocationSite)
		{
			if (sharedObjAllocSites.contains(objID)) return false;
		}
		
		return true;
	}

	public static boolean isSynchEventMethod(String methodSig)
	{
		if (methodSig.startsWith("java.lang.Thread.start")) return true;
		if (methodSig.startsWith("java.lang.Thread.join")) return true;
		if (methodSig.startsWith("java.lang.Object.wait")) return true;
		if (methodSig.startsWith("java.lang.Object.notify")) return true;
		
		return false;
	}

	public static boolean isJavaStandardLibraryMethod(String methodSig)
	{
		if (methodSig.startsWith("java.")) return true;
		if (methodSig.startsWith("javax.")) return true;

		if (methodSig.startsWith("com.ibm.wala.model.java.")) return true;

		return false;
	}

}
