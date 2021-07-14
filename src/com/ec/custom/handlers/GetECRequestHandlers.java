package com.ec.custom.handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

import cocom.leoch.sie.custom.oa.action.GetOADocAction;

public class GetECRequestHandlers extends AbstractHandler {

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
			// TODO Auto-generated catch block
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
		try {
			String oaid = null;
			oaid = ecn.getProperty("object_desc");
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
