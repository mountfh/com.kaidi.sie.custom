package com.leoch.sie.custom.sap.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.sap.action.BOPChangeSentToSAPAction;
import com.leoch.sie.custom.sap.action.BOPSentToSapAction;
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

public class BOPChangeSentToSapHandler extends AbstractHandler{


	@Override
	public Object execute(ExecutionEvent e) throws ExecutionException {
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(tcc instanceof TCComponentTask)) {
			MessageBox.post("请选择流程任务进行操作！", "提示", MessageBox.INFORMATION);
			return null;
		}

		TCComponentTask task = (TCComponentTask) tcc;
		try {
//			boolean checked = RuleCheck.check("EC", task);
//			if (!checked) {
//				MessageBox.post("当前任务不适用于BOM变更传SAP功能", "提示", MessageBox.INFORMATION);
//				return null;
//			}
			TCComponent[] targets = task.getRoot().getAttachments(TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
			TCComponentItem ecn = null;
			List<TCComponentItemRevision> solus = new ArrayList<>();
			for (int i = 0; i < targets.length; i++) {
				TCComponent target = targets[i];
				String type = target.getType();
				if (!"K8_EC".equals(type)) {
					continue;
				}
				ecn = (TCComponentItem) target;
				TCComponent[] rev = ecn.getRelatedComponents("K8_Solution");
				if (rev == null) {
					continue;
				}					
				for (int j = 0; j < rev.length; j++) {
					if (rev[j] instanceof TCComponentItemRevision) {
						TCComponentItemRevision part = (TCComponentItemRevision) rev[j];
						String part_type = part.getType();
						if (part_type.endsWith("GYRevision")) {
							solus.add(part);
						}
					}
				}
				break;
			}
			if (ecn == null) {
				MessageBox.post("任务目标下的没有ECN", "提示", MessageBox.INFORMATION);
				return null;
			}
			String ecn_no = ecn.getProperty("item_id");
//			boolean flag = ecn.getLogicalProperty(ECNModel.ECNSentSAPFlag);
//			flag = false;
//			if (flag) {
//				MessageBox.post("变更信息已同步SAP！", "提示", MessageBox.INFORMATION);
//				return null;
//			}
			if (solus.size() == 0) {
				TCSession session = (TCSession) AIFUtility.getDefaultSession();			
//				session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
				ecn.setLogicalProperty(ECNModel.ECNSentSAPFlag, true);
//				session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
				MessageBox.post("ECN中没有要同步SAP的工艺信息", "提示", MessageBox.INFORMATION);
				return null;
			}
			String msg = "";
			String temp = null;
			for (int i = 0; i < solus.size(); i++) {
				solus.get(i).refresh();
				temp = solus.get(i).getProperty("K8_Related_Part");
				if(temp.equals("")) {
					temp = solus.get(i).getProperty("object_name");
					msg += temp+":没有关联物料！"+"\n";
				}
			}
			if(!msg.equals("")) {
				MessageBox.post(msg, "提示", MessageBox.INFORMATION);
			}else {
				BOPChangeSentToSAPAction action = new BOPChangeSentToSAPAction(solus, ecn_no);
				action.excute();
			}			
		} catch (Exception exp) {
			MessageBox.post(exp);
			exp.printStackTrace();
		}
		return null;
	}


	
	
}