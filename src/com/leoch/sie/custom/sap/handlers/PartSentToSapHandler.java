package com.leoch.sie.custom.sap.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.sap.action.PartSyncToSapAction;
import com.leoch.sie.custom.sap.logs.NewPartLog;
import com.leoch.sie.custom.utils.RuleCheck;
import com.sap.conn.jco.JCoException;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class PartSentToSapHandler extends AbstractHandler {
	
	
	private Logger log = NewPartLog.logger;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(tcc instanceof TCComponentTask)) {
			MessageBox.post("请选择流程任务进行操作！", "提示", MessageBox.INFORMATION);
			return null;
		}
		AIFUtility.getCurrentApplication().getTargetContext().getParentComponent();
		TCComponentTask task = (TCComponentTask) tcc;
		try {
			boolean checked = RuleCheck.check("NewPart", task);
			if (!checked) {
				MessageBox.post("当前任务不适用于物料新建传SAP功能", "提示", MessageBox.INFORMATION);
				return null;
			}
			TCComponent[] targets = task.getRoot().getAttachments(TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
			PartSyncToSapAction action = new PartSyncToSapAction(targets, true, log);					
			action.excute();
		} catch (TCException | JCoException | IOException e) {
			log.error(e);
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;
	}
}
