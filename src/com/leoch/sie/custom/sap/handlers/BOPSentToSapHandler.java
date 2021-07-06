package com.leoch.sie.custom.sap.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.sap.action.BOMSentToSAPAction;
import com.leoch.sie.custom.sap.action.BOPSentToSapAction;
import com.leoch.sie.custom.utils.RuleCheck;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class BOPSentToSapHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent e) throws ExecutionException {
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(tcc instanceof TCComponentTask)) {
			MessageBox.post("请选择流程任务进行操作！", "提示", MessageBox.INFORMATION);
			return null;
		}

		TCComponentTask task = (TCComponentTask) tcc;
		try {
			boolean checked = RuleCheck.check("BOP", task);
			if (!checked) {
				MessageBox.post("当前任务不适用工艺表传SAP功能", "提示", MessageBox.INFORMATION);
				return null;
			}
			List<TCComponentItemRevision> revs = new ArrayList<>();
			TCComponent[] targets = task.getRoot().getAttachments(TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
			for (int i = 0; i < targets.length; i++) {
				TCComponent target = targets[i];
				if (target instanceof TCComponentItemRevision) {
					TCComponentItemRevision part = (TCComponentItemRevision) target;
					String part_type = part.getType();
					if (part_type.endsWith("GYRevision")) {
						revs.add(part);
					}
				}
			}
			if (revs == null || revs.size() == 0) {
				MessageBox.post("任务目标下的物料没有关联工艺对象！", "提示", MessageBox.INFORMATION);
				return null;
			}
			String msg = "";
			String temp = null;
			for (int i = 0; i < revs.size(); i++) {
				revs.get(i).refresh();
				temp = revs.get(i).getProperty("k8_MATNR2");
				if(temp.equals("")) {
					temp = revs.get(i).getProperty("object_name");
					msg += temp+":没有关联物料！"+"\n";
				}
			}
			if(!msg.equals("")) {
				MessageBox.post(msg, "提示", MessageBox.INFORMATION);
			}else {
				BOPSentToSapAction action = new BOPSentToSapAction(revs);
				action.excute();	
			}
		} catch (TCException exp) {
			MessageBox.post(exp);
			exp.printStackTrace();
		}
		
		return null;
	}

}

