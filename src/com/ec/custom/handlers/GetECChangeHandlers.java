package com.ec.custom.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

import cocom.leoch.sie.custom.oa.action.GetOADocAction;

public class GetECChangeHandlers extends AbstractHandler {

	TCSession session;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
				
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(tcc instanceof TCComponentTask)) {
			MessageBox.post("请选择流程任务进行操作！", "提示", MessageBox.INFORMATION);
			return null;
		}
		TCComponentTask task = (TCComponentTask) tcc;
		TCComponent[] targets = null;		
		try {
			targets = task.getRoot().getAttachments(TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		TCComponentItem ecn = null;		
		for (int i = 0; i < targets.length; i++) {
			TCComponent target = targets[i];
			String type = target.getType();
			if (!"K8_EC".equals(type)) {
				continue;
			}
			ecn = (TCComponentItem) target;
			break;
		}
		if(ecn==null){
			MessageBox.post("流程目标下没有EC对象，无法根据EC的OA流程单号，获取附件", "提示", MessageBox.INFORMATION);
			return null;
		}
		try {
			String oaid = null;
			oaid = ecn.getProperty("k8_OA");
			if(oaid.equals("")){
				MessageBox.post("EC没有流程单号，无法获取OA的归档附件，请先填写OA流程单号", "提示", MessageBox.INFORMATION);
			}
			GetOADocAction action = new GetOADocAction();
			String msg = action.sent(oaid,ecn);
			MessageBox.post(msg, "提示", MessageBox.INFORMATION);
		} catch (Exception e) {
			MessageBox.post(e.toString(), "错误", MessageBox.ERROR);
			e.printStackTrace();
		}		
		return null;
	}

}
