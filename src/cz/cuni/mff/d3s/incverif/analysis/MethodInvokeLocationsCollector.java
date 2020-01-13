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
package cz.cuni.mff.d3s.incverif.analysis;

import java.util.Iterator;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.IBytecodeMethod;
import com.ibm.wala.shrikeBT.*;
import com.ibm.wala.types.TypeReference;

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.wala.WALAContext;
import cz.cuni.mff.d3s.incverif.wala.WALAUtils;


public class MethodInvokeLocationsCollector
{
	// we need to have a sorted set of locations (program points)
	public TreeSet<ProgramPoint> mthInvokeLocs;

	public boolean considerJustMethodsWithoutResult;


	public MethodInvokeLocationsCollector(boolean considerMthWoRes)
	{
		mthInvokeLocs = new TreeSet<ProgramPoint>();

		considerJustMethodsWithoutResult = considerMthWoRes;
	}


	public void analyzeProgram(WALAContext walaCtx) throws Exception
	{
		for (IClass cls : walaCtx.classHierarchy)
		{
			for (IMethod mth : cls.getDeclaredMethods()) 
			{
				String mthSig = mth.getSignature();

				if (WALAUtils.isSyntheticMethod(mth)) continue;
				
				if (mth.isNative() || mth.isAbstract()) continue;

				IBytecodeMethod bcMth = (IBytecodeMethod) mth;

				IInstruction[] mthInstructions = bcMth.getInstructions();

				for (int insnIndex = 0; insnIndex < mthInstructions.length; insnIndex++)
				{
					Instruction insn = (Instruction) mthInstructions[insnIndex];

					int insnBcPos = WALAUtils.getInsnBytecodePos(mth, insnIndex);
					int insnBcIndex = WALAUtils.getInsnBytecodeIndex(mth, insnBcPos, walaCtx);

					ProgramPoint insnPP = new ProgramPoint(mthSig, insnIndex, insnBcPos, insnBcIndex);

					if (insn instanceof InvokeInstruction)
					{
						InvokeInstruction invokeInsn = (InvokeInstruction) insn;

						boolean skip = false;

						if (considerJustMethodsWithoutResult)
						{
							if (mth.getReturnType() != TypeReference.Void) skip = true;
						}

						if ( ! skip )
						{
							mthInvokeLocs.add(insnPP);
						}
					}
				}
			}
		}
	}

	public ProgramPoint findNearestInvokeLocationAfter(ProgramPoint inputPP)
	{
		return mthInvokeLocs.higher(inputPP);
	}

}

