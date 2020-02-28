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

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.io.File;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.IBytecodeMethod; 
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference; 
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference; 
import com.ibm.wala.shrikeBT.*;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.Utils;
import cz.cuni.mff.d3s.incverif.wala.jvm.*;
import cz.cuni.mff.d3s.incverif.JPFDecoratorFactory;


public class WALAUtils
{
	public static WALAContext initLibrary(String mainClassName, String targetClassPath, String walaExclusionFilePath) throws Exception
	{
		WALAContext walaCtx = new WALAContext();

		walaCtx.scope = createAnalysisScope(targetClassPath, walaExclusionFilePath);

		walaCtx.classHierarchy = makeClassHierarchy(walaCtx.scope);

		walaCtx.entryPoints = com.ibm.wala.ipa.callgraph.impl.Util.makeMainEntrypoints(walaCtx.scope, walaCtx.classHierarchy, Utils.getInternalClassName(mainClassName, false));

		walaCtx.options = new AnalysisOptions(walaCtx.scope, walaCtx.entryPoints);
		walaCtx.options.setHandleStaticInit(true);

		walaCtx.cache = new AnalysisCache();

		walaCtx.mthSig2BcPosIdx = createMapFromBytecodePositionToIndex(walaCtx.classHierarchy);

		return walaCtx;
	}

	private static AnalysisScope createAnalysisScope(String targetClassPath, String exclusionFilePath) throws Exception
	{
		JVMFactory jvmFactory = new JPFDecoratorFactory(AnalysisScopeReaderCustomJVM.getDefaultJVMFactory(), "externals/jpf-core/build/jpf-classes.jar");

		AnalysisScope scope = AnalysisScopeReaderCustomJVM.readJavaScope("wala-jpf.txt", new File(exclusionFilePath), WALAContext.class.getClassLoader(), jvmFactory);

		AnalysisScopeReaderCustomJVM.processScopeDefLine(scope, WALAContext.class.getClassLoader(), jvmFactory, "Application,Java,binaryDir," + targetClassPath);

		return scope;
	}
	
	private static IClassHierarchy makeClassHierarchy(AnalysisScope scope) throws Exception
	{	
		return ClassHierarchy.make(scope);
	}

	private static Map<String, Map<Integer, Integer>> createMapFromBytecodePositionToIndex(IClassHierarchy classHierarchy) throws Exception
	{
		Map<String, Map<Integer, Integer>> mthSig2BcPosIdx = new HashMap<String, Map<Integer, Integer>>();

		for (IClass cls : classHierarchy)
		{
			for (IMethod mth : cls.getDeclaredMethods())
			{
				String mthSig = mth.getSignature();

				if ( ! (mth instanceof IBytecodeMethod) ) continue;

				IBytecodeMethod bcMth = (IBytecodeMethod) mth;

				Map<Integer, Integer> mthBcPos2Idx = new HashMap<Integer, Integer>();
				mthSig2BcPosIdx.put(mthSig, mthBcPos2Idx);

				IInstruction[] mthInstructions = bcMth.getInstructions();

				if (mthInstructions == null) continue;

				int prevInsnBcPos = -1;
				int curInsnBcIndex = -1;

				for (int curInsnIdx = 0; curInsnIdx < mthInstructions.length; ++curInsnIdx)
				{
					int curInsnBcPos = getInsnBytecodePos(bcMth, curInsnIdx);

					// when two adjacent instructions have the same bytecode position, then we do not increment the bytecode index
					if (curInsnBcPos != prevInsnBcPos) curInsnBcIndex++;

					mthBcPos2Idx.put(curInsnBcPos, curInsnBcIndex);

					prevInsnBcPos = curInsnBcPos;
				}
			}
		}

		return mthSig2BcPosIdx;
	}

	public static void loadMethodNodesCache(WALAContext walaCtx)
	{
		for (CGNode node : walaCtx.callGraph)
		{
			String methodSig = node.getMethod().getSignature();
			
			walaCtx.mthSig2CGNode.put(methodSig, node);
		}
	}
	
	public static String getClassName(IClass cls, WALAContext walaCtx) throws Exception
	{
		if (cls == null) return null;
		
		String clsName = walaCtx.clsObj2Name.get(cls);
	
		if (clsName != null) return clsName;
		
		clsName = getTypeNameStr(cls.getName());
		
		walaCtx.clsObj2Name.put(cls, clsName);
					
		return clsName;
	}

	public static String getClassName(TypeReference typeRef, WALAContext walaCtx)
	{
		return getTypeNameStr(typeRef, walaCtx);
	}

	public static String getTypeNameStr(TypeName typeName) throws Exception
	{
		String internalTypeNameStr = typeName.toString();

		return Utils.getPlainTypeName(internalTypeNameStr);
	}
	
	public static String getTypeNameStr(TypeReference typeRef, WALAContext walaCtx)
	{
		if (typeRef == null) return null;
		
		if (walaCtx.typeRef2Name.containsKey(typeRef)) 
		{
			return walaCtx.typeRef2Name.get(typeRef);
		}
		
		try
		{
			String typeStr = getTypeNameStr(typeRef.getName());

			walaCtx.typeRef2Name.put(typeRef, typeStr);

			return typeStr;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	public static String getShortMethodName(MethodReference mthRef) throws Exception
	{
		return mthRef.getName().toUnicodeString();
	}

	public static String getDeclaringClassNameForField(FieldReference tgtField, WALAContext walaCtx) throws Exception
	{
		if (tgtField == null) return null;

		// input class name: loaded from bytecode instruction (field access)
		String inClassName = getClassName(tgtField.getDeclaringClass(), walaCtx);

		IClass tgtCls = null;

		// find the class which really declares the given field

		IClass cls = findClass(inClassName, walaCtx);

		// unknown class (e.g., due to exclusions)
		if (cls == null)
		{
			// we use the class name loaded from the bytecode instruction as the fallback result	
			return inClassName;	
		}

		while (cls != null)
		{
			for (IField fld : cls.getDeclaredStaticFields()) 
			{
				if (fld.getName().equals(tgtField.getName()))
				{
					tgtCls = cls;
					break;
				}
			}

			if (tgtCls != null) break;

			for (IField fld : cls.getDeclaredInstanceFields()) 
			{
				if (fld.getName().equals(tgtField.getName()))
				{
					tgtCls = cls;
					break;
				}
			}

			if (tgtCls != null) break;

			cls = cls.getSuperclass();
		}

		// unknown field name (this may happen, for example, when the field is defined and used only in the built-in model of a native method)
		if (tgtCls == null)
		{
			// we use the class name loaded from the bytecode instruction as the fallback result
			return inClassName;			
		}

		TypeName tgtClassType = tgtCls.getName();

		return getTypeNameStr(tgtClassType);
	}				
	
	public static String getDeclaringClassNameForField(IField field, WALAContext walaCtx) throws Exception
	{
		if (field == null) return null;
		
		return getDeclaringClassNameForField(field.getReference(), walaCtx);
	}

	public static String getDeclaringClassNameForMethod(MethodReference tgtMethod, WALAContext walaCtx) throws Exception
	{		
		String tgtMthSig = tgtMethod.getSignature();

		// input class name: loaded from bytecode instruction (method call)
		String inClassName = getClassName(tgtMethod.getDeclaringClass(), walaCtx);

		IClass tgtCls = null;

		// find the class which really declares the given method

		IClass cls = findClass(inClassName, walaCtx);

		// unknown class (e.g., due to exclusions)
		if (cls == null)
		{
			// we use the class name loaded from the bytecode instruction as the fallback result	
			return inClassName;	
		}

		while (cls != null)
		{
			for (IMethod mth : cls.getDeclaredMethods()) 
			{
				String mthSig = mth.getSignature();

				if (mthSig.equals(tgtMthSig))
				{
					tgtCls = cls;
					break;
				}
			}

			if (tgtCls != null) break;

			cls = cls.getSuperclass();
		}
	
		return getClassName(tgtCls, walaCtx);
	}

	public static IClass findClass(String tgtClassName, WALAContext walaCtx) throws Exception
	{
		if (tgtClassName.endsWith("[]")) return null;
		
		IClass cls = walaCtx.clsName2Obj.get(tgtClassName);

		if (cls != null) return cls;
		
		Iterator<IClass> clsIt = walaCtx.classHierarchy.iterator();
		while (clsIt.hasNext())
		{
			cls = clsIt.next();

			String clsName = getClassName(cls, walaCtx);

			if (tgtClassName.equals(clsName))
			{
				walaCtx.clsName2Obj.put(tgtClassName, cls);
				return cls;
			}
		}
		
		return null;
	}

	public static boolean isThreadClass(TypeReference typeRef, WALAContext walaCtx) throws Exception
	{
		String clsName = getClassName(typeRef, walaCtx);

		if (clsName.equals("java.lang.Thread")) return true;
		
		IClass cls = findClass(clsName, walaCtx);
	
		if (cls != null)
		{
			IClass clsSuper = cls.getSuperclass();

			while (clsSuper != null)
			{
				String clsSuperName = getClassName(clsSuper, walaCtx);

				if (clsSuperName.equals("java.lang.Thread")) return true;

				clsSuper = clsSuper.getSuperclass();
			}
		}

		return false;
	}
	
	public static IMethod findMethod(String tgtMethodSig, WALAContext walaCtx) throws Exception
	{
		CGNode mthNode = walaCtx.mthSig2CGNode.get(tgtMethodSig);

		if (mthNode != null) return mthNode.getMethod();

		// try looking into the class hierarchy

		String tgtClassName = Utils.extractClassName(tgtMethodSig);

		IClass tgtCls = findClass(tgtClassName, walaCtx);
		
		// find method with the given signature
		for (IMethod mth : tgtCls.getAllMethods())
		{
			String mthSig = mth.getSignature();
			
			if (tgtMethodSig.equals(mthSig)) return mth;
		}

		return null;
	}

	public static List<IMethod> findTargetMethods(CGNode curMthNode, SSAInvokeInstruction invokeInsn, CallGraph clGraph) throws Exception
	{
		List<IMethod> tgtMethods = new ArrayList<IMethod>();

		Set<CGNode> invokeTargetNodes = clGraph.getPossibleTargets(curMthNode, invokeInsn.getCallSite());

		for (CGNode invokeTgtMthNode : invokeTargetNodes)
		{
			tgtMethods.add(invokeTgtMthNode.getMethod());
		}

		return tgtMethods;
	}

	public static boolean isSyntheticMethod(IMethod mth)
	{
		if (mth.isSynthetic()) return true;

		if ( ! (mth instanceof IBytecodeMethod) ) return true;

		return false;
	}

	public static AllocationSite getAllocationSite(AllocationSiteInNode allocKey, WALAContext walaCtx)
	{
		IMethod mth = allocKey.getNode().getMethod();

		String methodSig = mth.getSignature();
				
		int insnIndex = allocKey.getSite().getProgramCounter();

		int insnBcPos = getInsnBytecodePos(mth, insnIndex);
		
		int insnBcIndex = getInsnBytecodeIndex(mth, insnBcPos, walaCtx);
	
		return new AllocationSite(methodSig, insnIndex, insnBcPos, insnBcIndex);
	}

	public static AllocationSite getAllocationSiteForClass(String className)
	{
		return new AllocationSite(className+".<clinit>", -1, -1, -1);
	}

	public static int getInsnBytecodePos(CGNode mthNode, int insnIndex)
	{
		return getInsnBytecodePos(mthNode.getMethod(), insnIndex);
	}

	public static int getInsnBytecodePos(IMethod mth, int insnIndex)
	{
		try
		{
			return ((IBytecodeMethod) mth).getBytecodeIndex(insnIndex);
		}
		catch (Exception ex)
		{
			return -1;
		}
	}

	public static int getInsnBytecodeIndex(CGNode mthNode, int insnBcPos, WALAContext walaCtx)
	{
		return getInsnBytecodeIndex(mthNode.getMethod(), insnBcPos, walaCtx);
	}

	public static int getInsnBytecodeIndex(IMethod mth, int insnBcPos, WALAContext walaCtx)
	{
		Map<Integer, Integer> mthBcPos2Idx = walaCtx.mthSig2BcPosIdx.get(mth.getSignature());

		if (mthBcPos2Idx == null) return -1;

		Integer insnBcIdx = mthBcPos2Idx.get(insnBcPos);

		if (insnBcIdx == null) return -1;

		return insnBcIdx.intValue();
	}

	public static ProgramPoint getPrecedingBytecodeInsnLocationByOffset(ProgramPoint inputPP, int offset, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(inputPP.methodSig, walaCtx);

		if (mth == null) return null;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		int newInsnIndex = inputPP.insnIndex - offset;

		int newInsnBcPos = getInsnBytecodePos(bcMth, newInsnIndex);
	
		int newInsnBcIndex = getInsnBytecodeIndex(bcMth, newInsnBcPos, walaCtx);

		return new ProgramPoint(inputPP.methodSig, newInsnIndex, newInsnBcPos, newInsnBcIndex);
	}

	public static ProgramPoint findOperandsLoadingStartBoundaryForBytecodeInsn(ProgramPoint inputPP, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(inputPP.methodSig, walaCtx);

		if (mth == null) return null;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		IInstruction[] mthInstructions = bcMth.getInstructions();

		Instruction inputInsn = (Instruction) mthInstructions[inputPP.insnIndex];

		// we start with the number of popped operands of the input bytecode instruction
		int numStackValuesToLoad = inputInsn.getPoppedCount();

		int curInsnIndex = inputPP.insnIndex;

		// we look for the sequence "goto ; astore" of bytecode instructions, where "astore" is the input bytecode instruction, that corresponds to an exception handler

		if (inputInsn instanceof StoreInstruction)
		{
			if (inputPP.insnIndex - 1 >= 0)
			{
				if (mthInstructions[inputPP.insnIndex - 1] instanceof GotoInstruction)
				{
					return inputPP;
				}
			}
		}

		// process bytecode instruction going backwards and track numbers of the popped/pushed operands/results
		// stop the loop just after processing all instructions that load operands for the input bytecode instruction
		while (numStackValuesToLoad > 0)
		{
			curInsnIndex--;

			Instruction curInsn = (Instruction) mthInstructions[curInsnIndex];

			if (curInsn instanceof DupInstruction)
			{
				// all variants of the DUP bytecode instructions are not handled correctly by the WALA ShrikeBT component

				numStackValuesToLoad--;
			}
			else
			{
				// covers also xLOAD or xCONST bytecode instructions
				if (curInsn.getPushedWordSize() > 0) numStackValuesToLoad--;

				// applies to xSTORE bytecode instructions and other instructions that manipulate with the stack (e.g., arithmetic, branching, and method invocations)
				numStackValuesToLoad += curInsn.getPoppedCount();
			}
		}

		// it should be always >= 0 because the desired start boundary instruction should be present
		int newInsnIndex = curInsnIndex;

		int newInsnBcPos = getInsnBytecodePos(bcMth, newInsnIndex);

		int newInsnBcIndex = getInsnBytecodeIndex(bcMth, newInsnBcPos, walaCtx);

		return new ProgramPoint(inputPP.methodSig, newInsnIndex, newInsnBcPos, newInsnBcIndex);
	}

	public static LinkedList<ProgramPoint> findResultsConsumingEndBoundaryForBytecodeInsn(ProgramPoint inputPP, boolean includeJumps, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(inputPP.methodSig, walaCtx);

		if (mth == null) return null;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		IInstruction[] mthInstructions = bcMth.getInstructions();

		LinkedList endBoundaryPPs = new LinkedList<ProgramPoint>();

		Instruction inputInsn = (Instruction) mthInstructions[inputPP.insnIndex];

		// we start with the result of the input bytecode instruction
		int numStackValuesToConsume = (inputInsn.getPushedWordSize() > 0) ? 1 : 0;

		int curInsnIndex = inputPP.insnIndex;

		// process bytecode instruction going forwards and track numbers of the popped/pushed operands/results
		// stop the loop just after processing all instructions that (transitively) consume results of the input bytecode instruction
		while (numStackValuesToConsume > 0)
		{
			curInsnIndex++;

			Instruction curInsn = (Instruction) mthInstructions[curInsnIndex];

			if (curInsn instanceof ConditionalBranchInstruction)
			{
				// we have to cover the whole sequence of instructions between the current one and the forward jump target (exclusively), because typically we cannot remove the jump instruction
					
				ConditionalBranchInstruction condbrInsn = (ConditionalBranchInstruction) curInsn;

				if (condbrInsn.getTarget() > curInsnIndex)
				{
					if (includeJumps)
					{
						// we have to record the conditional branching instruction that consumes some operands from the expression stack

						int condbrInsnIndex = curInsnIndex;
						int condbrInsnBcPos = getInsnBytecodePos(bcMth, condbrInsnIndex);
						int condbrInsnBcIndex = getInsnBytecodeIndex(bcMth, condbrInsnBcPos, walaCtx);
						ProgramPoint condbrInsnPP = new ProgramPoint(inputPP.methodSig, condbrInsnIndex, condbrInsnBcPos, condbrInsnBcIndex);

						endBoundaryPPs.add(condbrInsnPP);
					}

					curInsnIndex = condbrInsn.getTarget() - 1;

					Instruction targetPredInsn = (Instruction) mthInstructions[curInsnIndex];
	
					if (targetPredInsn instanceof GotoInstruction)
					{
						GotoInstruction gotoInsn = (GotoInstruction) targetPredInsn;

						if (gotoInsn.getLabel() > curInsnIndex)
						{
							curInsnIndex = gotoInsn.getLabel() - 1;
						}
					}
				}

				// process input operands of the conditional branching instruction
				numStackValuesToConsume -= curInsn.getPoppedCount();

				// this may happen if some operands for the conditional branching instruction are loaded before the input bytecode instruction
				// all the input operands for the bytecode instruction (that means including operands that make the number of values to be consumed negative) are covered by going backward and collect instructions for loading operands
				if (numStackValuesToConsume < 0) numStackValuesToConsume = 0;

				// target instruction of the jump is processed now
				curInsn = (Instruction) mthInstructions[curInsnIndex];
			}

			if (curInsn instanceof DupInstruction)
			{
				// all variants of the DUP bytecode instructions are not handled correctly by the WALA ShrikeBT component

				numStackValuesToConsume++;
			}
			else
			{
				// applies to xSTORE bytecode instructions and other instructions that manipulate with the stack (e.g., arithmetic, branching, and method invocations)
				numStackValuesToConsume -= curInsn.getPoppedCount();

				// this may happen if some operands for the instruction (arithmetic, for example) are loaded before the input bytecode instruction
				// all the input operands for the bytecode instruction (that means including operands that make the number of values to be consumed negative) are covered by going backward and collect instructions for loading operands
				if (numStackValuesToConsume < 0) numStackValuesToConsume = 0;

				// covers also xLOAD or xCONST bytecode instructions
				if (curInsn.getPushedWordSize() > 0) numStackValuesToConsume++;
			}
		}

		int newInsnIndex = curInsnIndex;

		int newInsnBcPos = getInsnBytecodePos(bcMth, newInsnIndex);

		int newInsnBcIndex = getInsnBytecodeIndex(bcMth, newInsnBcPos, walaCtx);

		// this program point has to be the first in the list (required by the caller)
		endBoundaryPPs.addFirst(new ProgramPoint(inputPP.methodSig, newInsnIndex, newInsnBcPos, newInsnBcIndex));

		return endBoundaryPPs;
	}

	public static boolean isMonitorExitBytecodeInsnWithinExceptionHandler(ProgramPoint inputPP, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(inputPP.methodSig, walaCtx);

		if (mth == null) return false;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		IInstruction[] mthInstructions = bcMth.getInstructions();

		Instruction inputInsn = (Instruction) mthInstructions[inputPP.insnIndex];

		// we look for the sequence "goto ; astore ; aload ; monitorexit" of bytecode instructions, where "goto" does not jump to "aload"

		if ( (inputInsn instanceof MonitorInstruction) && (inputInsn.getOpcode() == com.ibm.wala.shrikeBT.Constants.OP_monitorexit) )
		{
			if (inputPP.insnIndex - 1 < 0) return false;
			if ( ! (mthInstructions[inputPP.insnIndex - 1] instanceof LoadInstruction) ) return false;
	
			if (inputPP.insnIndex - 2 < 0) return false;
			if ( ! (mthInstructions[inputPP.insnIndex - 2] instanceof StoreInstruction) ) return false;
	
			if (inputPP.insnIndex - 3 < 0) return false;
			if ( ! (mthInstructions[inputPP.insnIndex - 3] instanceof GotoInstruction) ) return false;
			if (mthInstructions[inputPP.insnIndex - 3] instanceof GotoInstruction)
			{
				GotoInstruction gotoInsn = (GotoInstruction) mthInstructions[inputPP.insnIndex - 3];
				if ( gotoInsn.getLabel() == (inputPP.insnIndex - 1) ) return false;
			}

			return true;
		}

		return false;
	}
	
	public static ProgramPoint getMethodLastInsnLocation(String methodSig, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(methodSig, walaCtx);

		if (mth == null) return null;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		IInstruction[] mthInstructions = bcMth.getInstructions();
	
		int lastInsnIndex = mthInstructions.length - 1;

		int lastInsnBcPos = getInsnBytecodePos(bcMth, lastInsnIndex);

		int lastInsnBcIndex = getInsnBytecodeIndex(bcMth, lastInsnBcPos, walaCtx);

		return new ProgramPoint(methodSig, lastInsnIndex, lastInsnBcPos, lastInsnBcIndex);
	}
	
	public static int getInsnBytecodeSize(String methodSig, int insnIndex,  WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(methodSig, walaCtx);

		if (mth == null) return 1;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		// we assume that the next bytecode instruction exists
			// it should be true for all the cases in which this operation is performed

		int curInsnBcPos = getInsnBytecodePos(bcMth, insnIndex);
		int nextInsnBcPos = getInsnBytecodePos(bcMth, insnIndex + 1);

		return (nextInsnBcPos - curInsnBcPos);
	}
	
	public static boolean isMethodCodeFragmentWithUnmatchedMonitorEnterExit(String methodSig, ProgramPoint startPP, ProgramPoint endPP, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(methodSig, walaCtx);

		if (mth == null) return false;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		IInstruction[] mthInstructions = bcMth.getInstructions();

		boolean visitedMonitorEnter = false;
		boolean visitedMonitorExit = false;

		for (int curInsnIndex = startPP.insnIndex; curInsnIndex <= endPP.insnIndex; curInsnIndex++)
		{
			Instruction curInsn = (Instruction) mthInstructions[curInsnIndex];

			if ( (curInsn instanceof MonitorInstruction) && (curInsn.getOpcode() == com.ibm.wala.shrikeBT.Constants.OP_monitorenter) )
			{
				visitedMonitorEnter = true;
			}

			if ( (curInsn instanceof MonitorInstruction) && (curInsn.getOpcode() == com.ibm.wala.shrikeBT.Constants.OP_monitorexit) )
			{
				// there is no preceding "monitor enter" in the given bytecode range
				if ( ! visitedMonitorEnter ) return true;
			
				// we need to record only such "monitor exit" that is preceded by "monitor enter"
				if (visitedMonitorEnter)
				{
					visitedMonitorExit = true;
				}
			}
		}

		if ( visitedMonitorEnter && ( ! visitedMonitorExit ) ) return true;

		return false;
	}

	public static boolean isCodeFragmentRepresentingWholeMethod(String methodSig, ProgramPoint startPP, ProgramPoint endPP, WALAContext walaCtx) throws Exception
	{
		IMethod mth = findMethod(methodSig, walaCtx);

		if (mth == null) return false;

		IBytecodeMethod bcMth = (IBytecodeMethod) mth;

		IInstruction[] mthInstructions = bcMth.getInstructions();

		if ((startPP.insnIndex == 0) && (endPP.insnIndex == mthInstructions.length - 1)) return true;

		return false;
	}

	public static boolean isMethodReachableInThreadCallGraph(String entryMthSig, String targetMthSig, WALAContext walaCtx) throws Exception
	{
		// queue of method signatures to be processed
		LinkedList<String> mthWorklist = new LinkedList<String>();
		mthWorklist.add(entryMthSig);

		// set of methods that were already inspected
		Set<String> visitedMthSigs = new HashSet<String>();

		while ( ! mthWorklist.isEmpty() )
		{
			String curMthSig = mthWorklist.removeFirst();

			visitedMthSigs.add(curMthSig);

			IMethod curMth = findMethod(curMthSig, walaCtx);

			// loop over methods called from within the current one

			for ( CGNode curMthNode : walaCtx.callGraph.getNodes(curMth.getReference()) )
			{
				Iterator<CallSiteReference> callSitesIt = curMthNode.iterateCallSites();

				while (callSitesIt.hasNext())
				{
					CallSiteReference callSite = callSitesIt.next();

					for ( CGNode calleeNode : walaCtx.callGraph.getPossibleTargets(curMthNode, callSite) )
					{
						IMethod calleeMth = calleeNode.getMethod();

						String calleeMthSig = calleeMth.getSignature();

						// we should not cross thread start boundary
						if ( calleeMthSig.contains(".start()V") && walaCtx.classHierarchy.isSubclassOf(calleeMth.getDeclaringClass(), findClass("java.lang.Thread", walaCtx)) ) continue;


						if (targetMthSig.equals(calleeMthSig)) return true;

						if (visitedMthSigs.contains(calleeMthSig)) continue;

						mthWorklist.add(calleeMthSig);
					}
				}
			}
		}

		return false;
	}

}

