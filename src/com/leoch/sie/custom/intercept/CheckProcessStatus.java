package com.leoch.sie.custom.intercept;

import com.smile.interceptor.api.IWfHandler;
import com.smile.interceptor.api.WfNodeInfo;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentBOPWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class CheckProcessStatus implements IWfHandler{

	private final String PRG_VERISION = "V1.0";
	
	private WfNodeInfo nodeInfo = null;
	
	String info = null;
	
	public CheckProcessStatus() {
		initNodeInfo();
	}

	private void initNodeInfo() {

		nodeInfo = new WfNodeInfo();

		nodeInfo.prgVersion = PRG_VERISION;

		nodeInfo.enableNoDicession = false;

		nodeInfo.customizePrompt = "检查工艺结构文档状态";

	}

	@Override
	public WfNodeInfo getNodeInfo() {

		return nodeInfo;
	}

	@Override
	public String handleTask(TCComponentTask task, TCCRDecision decision) {
		String msg = null;
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		try {
			String taskType = task.getTaskType();
			if (taskType.equals("EPMDoTask") || decision.getIntValue() == 89) {
				TCComponent[] targets = task.getRelatedComponents("root_target_attachments");
				if (targets == null || targets.length < 1) {
					return msg;
				}
				for (int i = 0; i < targets.length; i++) {
					TCComponent target = targets[i];
					String type = target.getType();
					if (!"L8_ProcessRevision".equals(type) && !type.endsWith("processRevision")) {
						continue;
					}
					TCComponentItemRevision process = (TCComponentItemRevision) target;
					TCComponentBOPWindowType bopType = (TCComponentBOPWindowType) session.getTypeComponent("BOPWindow");
					TCComponentBOPWindow window = bopType.createBOPWindow(null);
					TCComponentBOMLine top = window.setWindowTopLine(process.getItem(), process, null, null);
					info = "";
					check(top);
					if (!info.isEmpty()) {
						if (msg != null) {
							msg += info;
						} else {
							msg = info;
						}
					}
					window.close();
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
			msg = e.toString();
		}
		return msg;
	}
	
	public void check(TCComponentBOMLine top) throws TCException {
		AIFComponentContext[] children = top.getChildren();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				TCComponentBOMLine subLine = (TCComponentBOMLine) children[i].getComponent();
				TCComponentItemRevision revision = subLine.getItemRevision();
				if (revision != null) {
					String type = revision.getType();
					if ("L8_DocumentRevision".equals(type)) {
						String rev_id = revision.getProperty("item_revision_id");
						if ("A0".equals(rev_id)) {
							String status = revision.getProperty("release_status_list");
							if (status.isEmpty()) {
								String id = revision.getProperty("item_id");
								info += id + "/" + rev_id + "状态不能为空" + "\n";
							}
						}
					}
					check(subLine);
				}
			}
		}
	}

}
