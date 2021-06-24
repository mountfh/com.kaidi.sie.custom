package com.leoch.sie.custom.intercept;

import com.smile.interceptor.api.IWfHandler;
import com.smile.interceptor.api.WfNodeInfo;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponentTask;

public class DocumentClassifyHandler implements IWfHandler {
	
	private final String PRG_VERISION = "V1.0";
	
	private WfNodeInfo nodeInfo = null;	

	public DocumentClassifyHandler() {
		initNodeInfo();
	}

	private void initNodeInfo() {

		nodeInfo = new WfNodeInfo();

		nodeInfo.prgVersion = PRG_VERISION;

		nodeInfo.enableNoDicession = false;

		nodeInfo.customizePrompt = "Í¼ÎÄµµ¹éµµ";

	}

	@Override
	public WfNodeInfo getNodeInfo() {

		return nodeInfo;
	}

	@Override
	public String handleTask(TCComponentTask task, TCCRDecision decision) {
		String msg = Handlers.classifyAndSentMail(task, decision);
		return msg;
	}		

}
