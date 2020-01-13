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

import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.types.TypeReference;


public class WALAContext
{
	// global context for analysis
	
	public IClassHierarchy classHierarchy;
	
	public AnalysisScope scope;
	public AnalysisOptions options;
	public AnalysisCache cache;
	
	public Iterable<Entrypoint> entryPoints;
	
	public CallGraph callGraph;
	public PointerAnalysis pointerAnalysis;
	
	public ExplodedInterproceduralCFG interprocCFG;

	public AllocationSitesData allocSitesData;

	public TypeAnalysisData typeData;

	// for each method signature, a map from bytecode positions to (real/actual) bytecode indexes
	public Map<String, Map<Integer, Integer>> mthSig2BcPosIdx;

	// useful caches

	public Map<String, CGNode> mthSig2CGNode;
	public Map<String, IClass> clsName2Obj;
	public Map<IClass, String> clsObj2Name;
	public Map<TypeReference, String> typeRef2Name;


	public WALAContext()
	{
		mthSig2CGNode = new HashMap<String, CGNode>();
		clsName2Obj = new HashMap<String, IClass>();
		clsObj2Name = new HashMap<IClass, String>();
		typeRef2Name = new HashMap<TypeReference, String>(); 
	}

}

