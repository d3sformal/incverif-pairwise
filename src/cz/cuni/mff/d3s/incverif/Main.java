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
package cz.cuni.mff.d3s.incverif;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.FileSystems;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder; 
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.CodeBlockBoundary;
import cz.cuni.mff.d3s.incverif.common.Utils;
import cz.cuni.mff.d3s.incverif.wala.WALAContext;
import cz.cuni.mff.d3s.incverif.wala.WALAUtils;
import cz.cuni.mff.d3s.incverif.wala.AllocationSitesData;
import cz.cuni.mff.d3s.incverif.wala.TypeAnalysisData;
import cz.cuni.mff.d3s.incverif.wala.ThreadEscapeAnalysis;
import cz.cuni.mff.d3s.incverif.analysis.FieldAccessCollector;
import cz.cuni.mff.d3s.incverif.analysis.ArrayObjectAccessCollector;
import cz.cuni.mff.d3s.incverif.analysis.SynchEventCollector;
import cz.cuni.mff.d3s.incverif.analysis.InterferingActionsCollector;
import cz.cuni.mff.d3s.incverif.analysis.MethodInvokeLocationsCollector;
import cz.cuni.mff.d3s.incverif.analysis.VariableUpdateLocationsCollector;
import cz.cuni.mff.d3s.incverif.pairwise.ThreadExecutionMonitor;

import cz.cuni.mff.d3s.multiver.MultiVerGenMain;
import cz.cuni.mff.d3s.multiver.SourceLocation;
import cz.cuni.mff.d3s.multiver.SourceLocations;


public class Main
{
	public static final int TIME_LIMIT_SEC = 60;


	public static void main(String[] args)
	{
		String mode = args[0];
	
		// prefix of the name of the directory into which multiple versions of the subject program (application) will be generated
		String genVersionsPathPrefixStr = args[1];

		// step 0: process the configuration for JPF and WALA

		// we have to make a clone of the command-line arguments because JPF may change them
		String[] cmdArgs = new String[args.length - 2];
		System.arraycopy(args, 2, cmdArgs, 0, args.length - 2);

		// load the part of configuration specified in build.xml
		Config jpfConfigBase = JPF.createConfig(cmdArgs);

		jpfConfigBase.setProperty("listener", "gov.nasa.jpf.listener.PreciseRaceDetector,cz.cuni.mff.d3s.incverif.tools.TimeConstrainedJPF"); //,cz.cuni.mff.d3s.incverif.tools.ThreadChoiceMonitor");
		jpfConfigBase.setProperty("jpf.time_limit", String.valueOf(TIME_LIMIT_SEC));
		jpfConfigBase.setProperty("race.exclude", "");

		if (mode.equals("alg1:thpairwise"))
		{
			jpfConfigBase.setProperty("vm.scheduler.sync.class", "cz.cuni.mff.d3s.incverif.pairwise.PairwiseSyncPolicy");
			jpfConfigBase.setProperty("vm.scheduler.sharedness.class", "cz.cuni.mff.d3s.incverif.pairwise.PairwiseSharednessPolicy");
		}

		// get the main class name from JPF configuration parameters (including command line)
		String mainClassName = jpfConfigBase.getProperty("target");
		if (mainClassName == null)
		{
			String[] freeArgs = jpfConfigBase.getFreeArgs();
			if (freeArgs != null) mainClassName = freeArgs[0];
		}

		Date analysisStartTime = new Date();

		// step 1: init the WALA library for static analysis

		// directory in which all the application classes are located
		String targetClassPathStr = jpfConfigBase.getString("analysis.target.dir", "");

		// exclusion file identifies library class that are ignored by the static analysis
		String walaExclusionFilePathStr = jpfConfigBase.getString("analysis.exclusion.file", "");

		WALAContext walaCtx = null;

		try
		{
			walaCtx = WALAUtils.initLibrary(mainClassName, targetClassPathStr, walaExclusionFilePathStr);
		}
		catch (Exception ex)
		{
			System.err.println("[ERROR] initialization failed");
			ex.printStackTrace();
			return;
		}

		// step 2: identify relevant "modified code fragments (units of change)" in the given subject program
			// every modified code fragment represents one possibly interfering action together with the directly affected statements (bytecode instructions)
			// we do this in two steps: (1) running the necessary static analyses to identify the possibly interfering actions, (2) followed by simple postprocessing to expand the bytecode ranges in order to cover all the directly affected bytecode instructions

		Set<CodeBlockBoundary> relevantModifiedCodeFragments = null;

		try
		{
			relevantModifiedCodeFragments = determineAffectedCodeBlocksForInterferingActions(walaCtx, mainClassName, targetClassPathStr, walaExclusionFilePathStr);
		}
		catch (Exception ex)
		{
			System.err.println("[ERROR] static analysis failed");
			ex.printStackTrace();
			return;
		}

		Date analysisFinishTime = new Date();
		
		long analysisUsedTimeInSec = computeTimeDiff(analysisStartTime, analysisFinishTime);

		long analysisUsedMemoryInMB = (Runtime.getRuntime().totalMemory() >> 20);
  		
		System.out.println("[ANALYSIS] time = " + analysisUsedTimeInSec + " s, memory = " + analysisUsedMemoryInMB + " MB \n");

		// step 3: for every relevant "modified code fragment", generate the corresponding version of the subject program and run JPF upon it

		ExperimentsStats expStats = new ExperimentsStats();

		for (CodeBlockBoundary modifiedCBB : relevantModifiedCodeFragments)
		{
			// generate version of the subject program that does not contain the respective "modified code fragment"
			// we use the multiver generator tool implemented by Filip Kliber

			Path targetClassPathObj = FileSystems.getDefault().getPath(targetClassPathStr);

			// we have to define a separate unique directory name for every "modified code fragment", because the generator requires a completely empty target directory (where it puts the modified Java bytecode class files)
			
			String genVersionsPathUniqueStr = genVersionsPathPrefixStr + File.separator + modifiedCBB.hashCode();
			Path genVersionsPathUniqueObj = FileSystems.getDefault().getPath(genVersionsPathUniqueStr);

			// here we assume that both ends of the modified code fragment lie within the same method
			// otherwise the "modified code fragment" is not valid

			if ( ! modifiedCBB.isValid() ) continue;

			String cbbFullMethodSig = modifiedCBB.getMethodSignature();

			try
			{
				// we do not want to modify classes from the Java core standard library
				if (Utils.isJavaStandardLibraryMethod(cbbFullMethodSig)) continue;

				// we ignore code fragments that contain unmatched pairs of bytecode instructions "monitor enter" and "monitor exit"
				if (WALAUtils.isMethodCodeFragmentWithUnmatchedMonitorEnterExit(cbbFullMethodSig, modifiedCBB.startLoc, modifiedCBB.endLoc, walaCtx)) continue;

				// we ignore every code fragment that covers/represents all instructions within the given method
				if (WALAUtils.isCodeFragmentRepresentingWholeMethod(cbbFullMethodSig, modifiedCBB.startLoc, modifiedCBB.endLoc, walaCtx)) continue;

			}
			catch (Exception ex)
			{
				System.err.println("[ERROR] bytecode range inspection failed");
				ex.printStackTrace();
				return;
			}

			System.out.print("\n\n");
			System.out.println("[LOG] modifiedCBB: methodSig = " + modifiedCBB.getMethodSignature() + ", startLoc = (bcidx:" + modifiedCBB.startLoc.insnBcIndex + ",bcpos:" + modifiedCBB.startLoc.insnBcPos + "), endLoc = (bcidx:" + modifiedCBB.endLoc.insnBcIndex + ",bcpos:" + modifiedCBB.endLoc.insnBcPos + ")");
			System.out.println("[LOG] version unique directory = " + genVersionsPathUniqueStr + File.separator + "stripped");

			String cbbClassName = Utils.extractClassName(cbbFullMethodSig);
			String cbbMethodName = Utils.extractPlainMethodName(cbbFullMethodSig);
			String cbbMethodDesc = Utils.extractMethodDescriptor(cbbFullMethodSig);

			SourceLocations allSourceLocs = new SourceLocations();

			SourceLocation locModifiedCBB = new SourceLocation(cbbClassName.replace('.', '/'), cbbMethodName, cbbMethodDesc);

			// end location is exclusive so we have to increment the value by 1
			locModifiedCBB.addRange(modifiedCBB.startLoc.insnBcIndex, modifiedCBB.endLoc.insnBcIndex + 1);

			allSourceLocs.add(locModifiedCBB);

			try
			{
				MultiVerGenMain.run(targetClassPathObj, allSourceLocs, genVersionsPathUniqueObj);
			}
			catch (Exception ex)
			{
				System.err.println("[ERROR] multiple version generator failed");
				ex.printStackTrace();
				return;
			}

			CodeBlockBoundary additionCBB;
			CodeBlockBoundary deletionCBB;

			try
			{
				additionCBB = (CodeBlockBoundary) modifiedCBB.clone();

				// use the least other known CBB that includes/wraps the given modifiedCBB, if there is such for the method, or the whole method
				CodeBlockBoundary wrapperCBB = findLeastWrappingCBB(modifiedCBB, relevantModifiedCodeFragments, walaCtx);
				deletionCBB = (CodeBlockBoundary) wrapperCBB.clone();
			
				// we also have to shift the end location forward to accommodate for the removed sequence of bytecode instructions (that is determined by the given modifiedCBB)
	
				int newEndLocIndex = deletionCBB.endLoc.insnIndex - (modifiedCBB.endLoc.insnIndex - modifiedCBB.startLoc.insnIndex + 1);
				int newEndLocBcPos = deletionCBB.endLoc.insnBcPos - (modifiedCBB.endLoc.insnBcPos - modifiedCBB.startLoc.insnBcPos + WALAUtils.getInsnBytecodeSize(deletionCBB.endLoc.methodSig, modifiedCBB.endLoc.insnIndex, walaCtx));
				int newEndLocBcIndex = deletionCBB.endLoc.insnBcIndex - (modifiedCBB.endLoc.insnBcIndex - modifiedCBB.startLoc.insnBcIndex + 1);
				
				deletionCBB.endLoc = new ProgramPoint(deletionCBB.endLoc.methodSig, newEndLocIndex, newEndLocBcPos, newEndLocBcIndex); 
			}
			catch (Exception ex)
			{
				System.err.println("[ERROR] cloning failed");
				ex.printStackTrace();
				return;
			}

			// process all the pairs <T1, T2> of dynamic thread instances for every "modified code fragment"
				// here, T1 corresponds to the modified thread T and T2 represents the arbitrary other thread T_o

			// global maximum thread ID value over all JPF runs for the pairs <T1, T2>
			// we assume that the subject program runs at least two concurrent threads
			int globalMaxThreadID = 1;

			// we use two nested while loops
			// outer while loop over IDs of all threads (from 0 to the dynamic maximum ID computed on-the-fly) to get the ID represented by the symbol T1
			// inner while loop over IDs of all threads (from 0 to the dynamic maximum) to get the ID represented by the symbol T2

			int outerLoopThreadID = 0;
			int innerLoopThreadID = 0;

			while (outerLoopThreadID <= globalMaxThreadID)
			{
				while (innerLoopThreadID <= globalMaxThreadID)
				{
					System.out.print("\n");
					System.out.println("[LOG] globalMaxThreadID = " + globalMaxThreadID + ", outerLoopThreadID = " + outerLoopThreadID + ", innerLoopThreadID = " + innerLoopThreadID);

					if (innerLoopThreadID == outerLoopThreadID)
					{
						innerLoopThreadID++;
						continue;
					}
		
					// we have to keep the base configuration intact (since it will be used many times)
					Config jpfConfigFull = (Config) jpfConfigBase.clone();

					jpfConfigFull.setProperty("incverif.pairwise.thread.modified.id", String.valueOf(outerLoopThreadID));
					jpfConfigFull.setProperty("incverif.pairwise.thread.other.id", String.valueOf(innerLoopThreadID));
	
					// run JPF for the given pair of threads on the input subject program
						// if the particular dynamic thread instance determined by the outer while loop is not an instance of static thread T, then just a single thread interleaving will be explored (quick and easy solution for the purpose of experimental evaluation)
	
					// modified program version (current affected "modified code fragment" is removed, simulating deletion)

					System.out.println("[LOG] checking modified program version (current affected code fragment is removed, simulating deletion)");
					System.out.println("[LOG] deletionCBB: methodSig = " + deletionCBB.getMethodSignature() + ", startLoc = (bcidx:" + deletionCBB.startLoc.insnBcIndex + ",bcpos:" + deletionCBB.startLoc.insnBcPos + "), endLoc = (bcidx:" + deletionCBB.endLoc.insnBcIndex + ",bcpos:" + deletionCBB.endLoc.insnBcPos + ")");
		
					// update the JPF configuration to reflect the directory that contains the generated modified version of the subject program (without "modified code fragment")

					Config jpfConfigDeletion = (Config) jpfConfigFull.clone();

					jpfConfigDeletion.setProperty("classpath", ".," + genVersionsPathUniqueStr + File.separator + "stripped");
	
					globalMaxThreadID = checkProgramVersionByJPF(jpfConfigDeletion, globalMaxThreadID, outerLoopThreadID, deletionCBB, expStats);

					// original program version (current affected "modified code fragment" is present, simulating addition)

					System.out.println("[LOG] checking original program version (current affected code fragment is present, simulating addition)");
					System.out.println("[LOG] additionCBB: methodSig = " + additionCBB.getMethodSignature() + ", startLoc = (bcidx:" + additionCBB.startLoc.insnBcIndex + ",bcpos:" + additionCBB.startLoc.insnBcPos + "), endLoc = (bcidx:" + additionCBB.endLoc.insnBcIndex + ",bcpos:" + additionCBB.endLoc.insnBcPos + ")");

					// update the JPF configuration to reflect the directory that contains the received original version of the subject program (with "modified code fragment")
	
					Config jpfConfigAddition = (Config) jpfConfigFull.clone();

					jpfConfigAddition.setProperty("classpath", ".," + targetClassPathStr);
	
					globalMaxThreadID = checkProgramVersionByJPF(jpfConfigAddition, globalMaxThreadID, outerLoopThreadID, additionCBB, expStats);

					// prepare for the next iteration of the inner loop

					innerLoopThreadID++;
				}

				// prepare for the next iteration of the outer loop 
	
				outerLoopThreadID++;
	
				innerLoopThreadID = 0;
			}
		}

		long jpfSumRunningTime = 0;
		for (Long rt : expStats.jpfRunningTimes) jpfSumRunningTime += rt.longValue();
		long jpfAvgRunningTime = jpfSumRunningTime / expStats.jpfRunningTimes.size();

		System.out.print("\n\n");
		System.out.println("[JPF SUMMARY] total runs = " + expStats.jpfTotalCountRuns + ", timedout runs = " + expStats.jpfCountTimedoutRuns + ", average running time = " + jpfAvgRunningTime + " s \n");
	}

	private static Set<CodeBlockBoundary> determineAffectedCodeBlocksForInterferingActions(WALAContext walaCtx, String mainClassName, String targetClassPath, String walaExclusionFilePath) throws Exception
	{
		// just to keep the argument lists compact (avoid the prefix "walaCtx")
		IClassHierarchy classHierarchy = walaCtx.classHierarchy;
		AnalysisScope scope = walaCtx.scope;
		AnalysisOptions options = walaCtx.options;
		AnalysisCache cache = walaCtx.cache;  

		// build call graph and compute pointer analysis to identify heap objects (for aliasing)

		// standard context-insensitive exhaustive pointer analysis (andersen)
		SSAPropagationCallGraphBuilder cgBuilder = com.ibm.wala.ipa.callgraph.impl.Util.makeVanillaZeroOneCFABuilder(options, cache, classHierarchy, scope);
	
		walaCtx.callGraph = cgBuilder.makeCallGraph(options, null);

		WALAUtils.loadMethodNodesCache(walaCtx);
	
		walaCtx.pointerAnalysis = cgBuilder.getPointerAnalysis();

		walaCtx.allocSitesData = new AllocationSitesData();
		walaCtx.allocSitesData.initializeData(walaCtx);

		walaCtx.typeData = new TypeAnalysisData();

		walaCtx.interprocCFG = ExplodedInterproceduralCFG.make(walaCtx.callGraph);

		// simple static thread escape analysis that identifies possibly shared objects

		ThreadEscapeAnalysis thEscAnalysis = new ThreadEscapeAnalysis();
		thEscAnalysis.analyzeProgram(walaCtx);

		Set<AllocationSite> sharedObjects = thEscAnalysis.escapedAllocSites;

		// identify all possible interfering actions by static analysis

		// collect field accesses that may occur during program execution in a flow-insensitive way
		
		FieldAccessCollector fieldsColl = new FieldAccessCollector();
		fieldsColl.analyzeProgram(walaCtx);

		// collect array object accesses that may occur during program execution in a flow-insensitive way
	
		ArrayObjectAccessCollector arraysColl = new ArrayObjectAccessCollector();
		arraysColl.analyzeProgram(walaCtx);
	
		// collect synchronization events that may occur during program execution in a flow-insensitive way

		SynchEventCollector synchColl = new SynchEventCollector();
		synchColl.analyzeProgram(walaCtx);

		// we ignore actions on thread local objects

		fieldsColl.dropThreadLocalAccesses(sharedObjects);
		arraysColl.dropThreadLocalAccesses(sharedObjects);
		synchColl.dropThreadLocalEvents(sharedObjects);

		// compute necessary information about updates (write accesses) of variables and method calls (that return "void")

		VariableUpdateLocationsCollector updateColl = new VariableUpdateLocationsCollector();
		updateColl.analyzeProgram(walaCtx);

		MethodInvokeLocationsCollector invokeColl = new MethodInvokeLocationsCollector(true);
		invokeColl.analyzeProgram(walaCtx);

		// aggregating program points of all possibly interfering actions (statements)
		// for each interfering action, it returns the range of program points that represent boundaries of the affected code block

		InterferingActionsCollector actionsColl = new InterferingActionsCollector(walaCtx, fieldsColl, arraysColl, synchColl, updateColl, invokeColl);

		Set<CodeBlockBoundary> codeBlocks = actionsColl.getAffectedCodeBlockBoundaries();

		return codeBlocks;
	}

	private static int checkProgramVersionByJPF(Config jpfConfig, int oldGlobalMaxThreadID, int modifiedThreadID, CodeBlockBoundary modifiedCBB, ExperimentsStats expStats)
	{
		int newGlobalMaxThreadID = oldGlobalMaxThreadID;

		Date jpfStartTime = new Date();

		try
		{
			JPF jpf = new JPF(jpfConfig);

			// we use the listener ThreadExecutionMonitor to record thread IDs and determine the maximum possible dynamic thread ID
				// parameters: ID of the modified thread, boundaries of the modified code fragment (two program points)
			ThreadExecutionMonitor thExecMon = new ThreadExecutionMonitor(jpfConfig, modifiedThreadID, modifiedCBB);

			jpf.addListener(thExecMon);

			jpf.run();

			// get the updated maximum thread ID at the end of each JPF run
			newGlobalMaxThreadID = thExecMon.getMaxThreadID();
		}
		catch (Exception ex)
		{
			System.err.println("[ERROR] cannot start JPF");
			ex.printStackTrace();
			if (ex.getCause() != null) ex.getCause().printStackTrace();
		}

		Date jpfFinishTime = new Date();

		long jpfUsedTimeInSec = computeTimeDiff(jpfStartTime, jpfFinishTime);

		System.out.println("[JPF] time = " + jpfUsedTimeInSec + " s \n");

		expStats.jpfTotalCountRuns++;

		if (jpfUsedTimeInSec >= TIME_LIMIT_SEC)
		{
			expStats.jpfCountTimedoutRuns++;
		}
		else
		{
			expStats.jpfRunningTimes.add(jpfUsedTimeInSec);
		}
	
		return newGlobalMaxThreadID;
	}

	private static CodeBlockBoundary findLeastWrappingCBB(CodeBlockBoundary inputCBB, Set<CodeBlockBoundary> candidateCBBs, WALAContext walaCtx) throws Exception
	{
		CodeBlockBoundary wrapperCBB = null;

		for (CodeBlockBoundary cbb : candidateCBBs)
		{
			// belongs to the same method
			if (inputCBB.startLoc.methodSig.equals(cbb.startLoc.methodSig))
			{
				// we are looking for CBB other than the input CBB
				if (inputCBB.equals(cbb)) continue;

				// wraps the input CBB
				if ( (cbb.startLoc.compareTo(inputCBB.startLoc) <= 0) && (cbb.endLoc.compareTo(inputCBB.endLoc) >= 0) )
				{
					// first discovered wrapping CBB
					if (wrapperCBB == null)
					{
						wrapperCBB = cbb;
					}
					else
					{
						// better fit than all the previously discovered wrapping CBBs
						if ( (cbb.startLoc.compareTo(wrapperCBB.startLoc) >= 0) && (cbb.endLoc.compareTo(wrapperCBB.endLoc) <= 0) )
						{
							wrapperCBB = cbb;
						}
					}
				}
			}
		}

		// there is no wrapping CBB in the set of candidates
		if (wrapperCBB == null)
		{
			// use the whole method
	
			ProgramPoint mthStartPP = new ProgramPoint(inputCBB.startLoc.methodSig, 0, 0, 0);

			ProgramPoint mthEndPP = WALAUtils.getMethodLastInsnLocation(inputCBB.startLoc.methodSig, walaCtx);

			wrapperCBB = new CodeBlockBoundary(mthStartPP, mthEndPP);
		}

		return wrapperCBB;
	}

	private static long computeTimeDiff(Date start, Date finish)
	{
		long startMS = start.getTime();
		long finishMS = finish.getTime();
	
		long diffMS = finishMS - startMS;
    	
		long diffSeconds = (diffMS / 1000);
    	
		return diffSeconds;
	}
	
	private static String printTimeDiff(Date start, Date finish)
	{
		long diff = computeTimeDiff(start, finish);
		return String.valueOf(diff);
	}


	static class ExperimentsStats
	{
		public int jpfTotalCountRuns = 0;
		public int jpfCountTimedoutRuns = 0;
		public List<Long> jpfRunningTimes = new ArrayList<Long>();
	}

}
