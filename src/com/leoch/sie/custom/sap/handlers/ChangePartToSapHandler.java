package com.leoch.sie.custom.sap.handlers;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.sap.action.PartSyncToSapAction;
import com.leoch.sie.custom.sap.logs.ChangePartLog;
import com.leoch.sie.custom.utils.Demo;
import com.leoch.sie.custom.utils.RuleCheck;
import com.sap.conn.jco.JCoException;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class ChangePartToSapHandler extends AbstractHandler {
	
	public Demo demo;

	private Logger log = ChangePartLog.logger;
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if (!(tcc instanceof TCComponentTask)) {
			MessageBox.post("请选择流程任务进行操作！", "提示", MessageBox.INFORMATION);
			return null;
		}

		TCComponentTask task = (TCComponentTask) tcc;
		try {
			boolean checked = RuleCheck.check("ChangePart", task);
			if (!checked) {
				MessageBox.post("当前任务不适用于物料变更传SAP功能", "提示", MessageBox.INFORMATION);
				return null;
			}
			final TCComponent[] targets = task.getRoot().getAttachments(TCAttachmentScope.LOCAL, TCAttachmentType.TARGET);
			demo = new Demo();
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					PartSyncToSapAction action = new PartSyncToSapAction(targets, false, log);
					try {
						
						demo.start();
						action.excute();
						demo.close();
						
					} catch (TCException e) {
						e.printStackTrace();
					} catch (JCoException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		} catch (TCException e) {
			log.error(e.toString());
			MessageBox.post(e);
			e.printStackTrace();
		}
		return null;
	}

}
