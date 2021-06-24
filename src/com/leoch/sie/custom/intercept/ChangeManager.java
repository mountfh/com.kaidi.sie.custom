package com.leoch.sie.custom.intercept;

import com.smile.interceptor.api.IWfHandler;
import com.smile.interceptor.api.WfNodeInfo;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponentTask;

/**
 * @author wall
 *
 */
public class ChangeManager implements IWfHandler {

	private final String PRG_VERISION = "V1.0";
	private WfNodeInfo nodeInfo = null;
	
	public ChangeManager() {
		initNodeInfo();
	}
	
	private void initNodeInfo() {
		
		nodeInfo = new WfNodeInfo();
		
		//当前程序版本
		nodeInfo.prgVersion = PRG_VERISION;
		
		//对话框标题
		nodeInfo.dlgTitle = "审核";
		
		//同意选项上的标签文字
		nodeInfo.aprovalPrompt = "批准";
		
		nodeInfo.rejectPrompt = "拒绝";
		
		//允许“不作决定”选项
		nodeInfo.enableNoDicession = false;
		
		//在注释框内填给出提示信息
		nodeInfo.customizePrompt = "选择状态";
	}
	
	public WfNodeInfo getNodeInfo() {
		
		return nodeInfo;
	}



	public String handleTask(TCComponentTask task, TCCRDecision decision) {	
		String msg = Handlers.changeStatus(task, decision);

		return msg;
	}

}
