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
package cz.cuni.mff.d3s.incverif.wala;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.analysis.typeInference.TypeInference;


public class TypeAnalysisData
{
	// map from a method signature to the object representing type data (inference)
	private Map<String, TypeInference> mthSig2TypeInfo;


	public TypeAnalysisData()
	{
		mthSig2TypeInfo = new HashMap<String, TypeInference>();
	}

	public TypeInference getMethodTypeInfo(String mthSig, IR mthIR)
	{
		TypeInference typeInfo = mthSig2TypeInfo.get(mthSig);

		if (typeInfo == null)
		{
			typeInfo = TypeInference.make(mthIR, true);

			mthSig2TypeInfo.put(mthSig, typeInfo);
		}

		return typeInfo;		
	}

}

