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

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.analysis.typeInference.TypeInference; 

import cz.cuni.mff.d3s.incverif.common.ProgramPoint;
import cz.cuni.mff.d3s.incverif.common.AllocationSite;
import cz.cuni.mff.d3s.incverif.common.SynchEventID;
import cz.cuni.mff.d3s.incverif.common.SynchEventType;
import cz.cuni.mff.d3s.incverif.common.Utils;


public class SynchEventCodeInfo
{
	public static Set<SynchEventID> getSynchEventsForInsn(CGNode curMthNode, IR curMthIR, SSAInstruction insn, WALAContext walaCtx) throws Exception
	{
		Set<SynchEventID> events = new HashSet<SynchEventID>();

		String curMthSig = curMthNode.getMethod().getSignature();

		// we ignore synchronization events inside Object.wait() and Thread.join()
		if (Utils.isSynchEventMethod(curMthSig)) return events;

		if (insn instanceof SSAInvokeInstruction)
		{
			SSAInvokeInstruction invokeInsn = (SSAInvokeInstruction) insn;

			String targetMthName = WALAUtils.getShortMethodName(invokeInsn.getDeclaredTarget());

			List<IMethod> targetMths = WALAUtils.findTargetMethods(curMthNode, invokeInsn, walaCtx.callGraph);
			
			SynchEventType evType = null;
			if (targetMthName.equals("wait")) evType = SynchEventType.WAIT;
			if (targetMthName.equals("notify") || targetMthName.equals("notifyAll")) evType = SynchEventType.NOTIFY;
			if (targetMthName.equals("start"))
			{
				if (WALAUtils.isThreadClass(invokeInsn.getDeclaredTarget().getDeclaringClass(), walaCtx)) evType = SynchEventType.THSTART;
			}
			if (targetMthName.equals("join"))
			{
				if (WALAUtils.isThreadClass(invokeInsn.getDeclaredTarget().getDeclaringClass(), walaCtx)) evType = SynchEventType.THJOIN;
			}

			if (evType != null)
			{
				// here we do not have to check for static methods because none of the relevant methods is actually static (Object.wait, Object.notify, Thread.start, Thread.join), so there must be a receiver object

				String rcvDeclClassName = WALAUtils.getDeclaringClassNameForMethod(invokeInsn.getDeclaredTarget(), walaCtx);

				Set<AllocationSite> rcvObjectAllocSites = walaCtx.allocSitesData.getAllocSitesForObject(curMthSig, invokeInsn.getReceiver(), rcvDeclClassName);

				for (AllocationSite objAS : rcvObjectAllocSites) events.add(new SynchEventID(objAS, evType));
			}

			if (targetMths != null)
			{
				for (IMethod tgtMth : targetMths)
				{
					if (tgtMth.isSynchronized())
					{
						String tgtMthDeclClassName = WALAUtils.getDeclaringClassNameForMethod(tgtMth.getReference(), walaCtx);

						if (tgtMth.isStatic())
						{
							events.add(new SynchEventID(WALAUtils.getAllocationSiteForClass(tgtMthDeclClassName), SynchEventType.LOCK_METHODRCV));
							events.add(new SynchEventID(WALAUtils.getAllocationSiteForClass(tgtMthDeclClassName), SynchEventType.UNLOCK_METHODRCV));
						}
						else
						{
							Set<AllocationSite> rcvObjectAllocSites = walaCtx.allocSitesData.getAllocSitesForObject(curMthSig, invokeInsn.getReceiver(), tgtMthDeclClassName);

							for (AllocationSite objAS : rcvObjectAllocSites)
							{
								events.add(new SynchEventID(objAS, SynchEventType.LOCK_METHODRCV));
								events.add(new SynchEventID(objAS, SynchEventType.UNLOCK_METHODRCV));
							}
						}
					}
				}
			}
		}

		if (insn instanceof SSAMonitorInstruction)
		{
			SSAMonitorInstruction monInsn = (SSAMonitorInstruction) insn;

			SynchEventType evType = null;
			if (monInsn.isMonitorEnter()) evType = SynchEventType.LOCK_ANYOBJECT;
			else evType = SynchEventType.UNLOCK_ANYOBJECT;

			TypeInference mthVarTypeInfo = walaCtx.typeData.getMethodTypeInfo(curMthSig, curMthIR);

			TypeReference tgtObjTypeRef = mthVarTypeInfo.getType(monInsn.getRef()).getTypeReference(); 
			String tgtObjClassName = WALAUtils.getClassName(tgtObjTypeRef, walaCtx);

			// process special case: "synchronized (<class name>.class)"
			if (tgtObjClassName.equals("java.lang.Class"))
			{
				// probably expensive but used rarely
				DefUse mthDU = new DefUse(curMthIR);

				SSAInstruction tgtObjDefInsn = mthDU.getDef(monInsn.getRef());

				// def insn for monInsn.getRef() should be ldc_w
				if (tgtObjDefInsn instanceof SSALoadMetadataInstruction)
				{
					SSALoadMetadataInstruction metaInsn = (SSALoadMetadataInstruction) tgtObjDefInsn;

					// get class name from the constant pool
					String lvarClassName = WALAUtils.getClassName((TypeReference) metaInsn.getToken(), walaCtx);

					events.add(new SynchEventID(WALAUtils.getAllocationSiteForClass(lvarClassName), evType));
				}
			}

			Set<AllocationSite> tgtObjectAllocSites = walaCtx.allocSitesData.getAllocSitesForObject(curMthSig, monInsn.getRef(), tgtObjClassName);

			for (AllocationSite objAS : tgtObjectAllocSites) events.add(new SynchEventID(objAS, evType));
		}

		return events;
	}	 
}

