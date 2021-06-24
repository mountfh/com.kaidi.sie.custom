package com.leoch.sie.custom.sap.intercept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.leoch.sie.custom.utils.SAPConn;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoTable;
import com.smile.interceptor.api.IWfHandler;
import com.smile.interceptor.api.WfNodeInfo;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;

/**
 * @author wall
 *
 */
public class DesignSentToSAP implements IWfHandler {

	private final String PRG_VERISION = "V1.0";
	private WfNodeInfo nodeInfo = null;
	
	public DesignSentToSAP() {
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
		nodeInfo.customizePrompt = "图纸信息同步到SAP";
	}
	
	public WfNodeInfo getNodeInfo() {
		
		return nodeInfo;
	}



	public String handleTask(TCComponentTask task, TCCRDecision decision) {	
		String msg = "";
		try {
			String taskType = task.getTaskType();
			if (taskType.equals("EPMDoTask") || decision.getIntValue() == 89) {
				TCComponent[] targets = task.getRelatedComponents("root_target_attachments");
				if (targets == null || targets.length < 1) {
					return null;
				} 
				List<TCComponentItemRevision> designs = new ArrayList<>();
				for (int i = 0; i < targets.length; i++) {
					String type = targets[i].getType();
					if (type.equals("L8_DesignRevision")) {
						TCComponentItemRevision rev = (TCComponentItemRevision) targets[i];
						designs.add(rev);
					} else if ("L8_ECN".equals(type)) {
						designs = new ArrayList<>();
						TCComponent[] solus = targets[i].getRelatedComponents("L8_SoluObject");
						for (int j = 0; j < solus.length; j++) {
							type = solus[j].getType();
							if (type.equals("L8_DesignRevision")) {
								TCComponentItemRevision rev = (TCComponentItemRevision) solus[j];
								designs.add(rev);
							} 
						}
						break;
					}
				}
				if (designs.size() == 0) {
					return null;
				}
				JCoDestination destination = SAPConn.connect();
				JCoRepository repository = destination.getRepository();
				JCoFunction function = repository.getFunction("ZFTUZHI");
				JCoTable inputTable = function.getTableParameterList().getTable("T_ZFTUZHI");
				for (int i = 0; i < designs.size(); i++) {
					String id = designs.get(i).getProperty("item_id");
					String rev_id = designs.get(i).getProperty("item_revision_id");
					inputTable.insertRow(i);
					inputTable.setValue("ZEINR", id);
					inputTable.setValue("BANCHI", rev_id);
				}
				function.execute(destination);
				JCoParameterList tableParams = function.getTableParameterList();
				JCoTable outputTable = tableParams.getTable("T_MESSAGE");
				int rows = outputTable.getNumRows();
				if (rows > 0) {
					outputTable.firstRow();
					for (int i = 0; i < rows; i++) {
						String type = outputTable.getString("TYPE");
						if (!type.equals("S")) {
							msg += outputTable.getString("MESSAGE") + "\n";
						}
						outputTable.nextRow();
					}					
				}
			}
		} catch (TCException | JCoException | IOException e) {
			e.printStackTrace();
			return e.toString();
		}
		if (msg.isEmpty()) {
			msg = null;
		}
		return msg;
	}

}
