package com.ec.custom.handlers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.sap.action.BOMSentToSAPAction;
import com.leoch.sie.custom.sap.action.ECNSentToSapAction;
import com.leoch.sie.custom.sap.models.ECNModel;
import com.leoch.sie.custom.utils.RuleCheck;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

import cocom.leoch.sie.custom.oa.action.ECMsgSentToOAAction;

public class SendECMsgHandlers extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent e) throws ExecutionException {
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(tcc instanceof TCComponentTask)) {
			MessageBox.post("请选择流程任务进行操作！", "提示", MessageBox.INFORMATION);
			return null;
		}

		TCComponentTask task = (TCComponentTask) tcc;
		try {
			boolean checked = RuleCheck.check("OA", task);
			if (!checked) {
				MessageBox.post("当前任务不适用于EC变更信息传OA功能", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponent[] targets = task.getRoot().getAttachments(TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
			TCComponentItem ecn = null;
			List<TCComponentItemRevision> solus = new ArrayList<>();
			List<TCComponentItemRevision> problems = new ArrayList<>();
			for (int i = 0; i < targets.length; i++) {
				TCComponent target = targets[i];
				String type = target.getType();
				if (!"K8_EC".equals(type)) {
					continue;
				}
				ecn = (TCComponentItem) target;
				TCComponent[] soluRev = ecn.getRelatedComponents("K8_Solution");
				TCComponent[] problemRev = ecn.getRelatedComponents("K8_Problem");
				if (soluRev == null) {
					continue;
				}					
				for (int j = 0; j < soluRev.length; j++) {
					if (soluRev[j] instanceof TCComponentItemRevision) {
						TCComponentItemRevision part = (TCComponentItemRevision) soluRev[j];
						String part_type = part.getType();
						if (part_type.endsWith("Revision")||part_type.endsWith("AssemblyRevision")) {
							solus.add(part);
						}
					}
				}
				if(problemRev.length>0) {
					for (int j = 0; j < problemRev.length; j++) {
						if(problemRev[j] instanceof TCComponentItemRevision) {
							TCComponentItemRevision part = (TCComponentItemRevision) problemRev[j];
							String part_type = part.getType();
							if (part_type.endsWith("Revision")||part_type.endsWith("AssemblyRevision")) {
								problems.add(part);
							}
						}
					}
				}
				break;
			}
			if (ecn == null) {
				MessageBox.post("任务目标下的没有EC", "提示", MessageBox.INFORMATION);
				return null;
			}
			if (solus.size() == 0) {
				TCSession session = (TCSession) AIFUtility.getDefaultSession();
//				session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
				ecn.setLogicalProperty(ECNModel.ECNSentSAPFlag, true);
//				session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
				MessageBox.post("EC中没有同步SAP的信息", "提示", MessageBox.INFORMATION);
				return null;
			}
			 String oaid = ecn.getProperty("k8_OA");
			 if(oaid.equals("")){
				 MessageBox.post("EC没有流程单号，无法获取OA的归档附件，请先填写OA流程单号", "提示", MessageBox.INFORMATION);
				 return null;
			 }
			 System.out.println(new Date());
			 ECMsgSentToOAAction action = new ECMsgSentToOAAction(ecn,problems, solus);
			 String msg=action.excute();
			 System.out.println(new Date());
			MessageBox.post(msg, "提示", MessageBox.INFORMATION);
		} catch (Exception exp) {
			MessageBox.post(exp);
			exp.printStackTrace();
		}
		return null;
	}

}
