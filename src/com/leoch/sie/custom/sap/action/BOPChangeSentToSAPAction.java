package com.leoch.sie.custom.sap.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.leoch.sie.custom.sap.models.BOMStruct;
import com.leoch.sie.custom.sap.models.BOPInfoModel;
import com.leoch.sie.custom.sap.models.BOPLineModel;
import com.leoch.sie.custom.utils.SAPConn;
import com.leoch.sie.custom.utils.Sort;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class BOPChangeSentToSAPAction {

	private List<TCComponentItemRevision> revs;
	private String ecn_no;
	TCSession session;
	//创建
	public static String functionName1 = "ZFUNC_001";
	//变更
	public static String functionName = "ZFUNC_003";
	
	public static String input_BOM_HDR = "I_INPUT";
	
	public static String input_BOM_ITEM = "T_TAB";
	
	public static String export_Table = "E_OUTPUT";
	
	
	public BOPChangeSentToSAPAction(List<TCComponentItemRevision> revs,String ecn_no) {
		this.revs = revs;
		this.ecn_no = ecn_no;
	}
	
	public void excute() {
		
		try {
			List<TCComponentItemRevision> newrev = new ArrayList<TCComponentItemRevision>();
			List<TCComponentItemRevision> changerev = new ArrayList<TCComponentItemRevision>();
			session = (TCSession) AIFUtility.getDefaultSession();
			String msg = "";
			String rev_id = null;
			for (int i = 0; i < revs.size(); i++) {
				rev_id = revs.get(i).getProperty("item_revision_id");
				if(rev_id.equals("A")) {
					newrev.add(revs.get(i));
				}else {
					changerev.add(revs.get(i));
				}
			}
			//存在新的工艺，先传送
			if(newrev.size()>0) {
				BOMStruct struct = new BOMStruct(revs, session);
				msg = struct.loadBOP();
				struct.close();
				if (!msg.isEmpty()) {
					//	log.error(msg);
						MessageBox.post(msg, "提示", MessageBox.INFORMATION);
						return;
					}
				Map<String, BOPInfoModel> models = struct.getBOPInfo();
				msg = sent(models,functionName1);
				if (msg != null && !msg.isEmpty()) {
					MessageBox.post(msg, "错误", MessageBox.ERROR);
					return;
				}
			}	
			//变更工艺传送sap
			if(changerev.size()>0) {
				BOMStruct struct = new BOMStruct(changerev, session,ecn_no); 
				msg = struct.loadBOP();
				struct.close();
				if (!msg.isEmpty()) {
		//			log.error(msg);
					MessageBox.post(msg, "提示", MessageBox.INFORMATION);
					return;
				}
				Map<String, BOPInfoModel> models = struct.getBOPInfo();
				if (models.size() == 0) {
					MessageBox.post("任务目标下没有需要同步SAP的BOM！", "提示", MessageBox.INFORMATION);
					return;
				}
				msg = sent(models,functionName);
				if (msg != null && !msg.isEmpty()) {
					MessageBox.post(msg, "错误", MessageBox.ERROR);
					return;
				}
			}
			MessageBox.post("工艺表变更发送SAP成功", "提示", MessageBox.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			//log.error(e);
			MessageBox.post(e);
		}		
	}	

	public String sent(Map<String, BOPInfoModel> models,String sapFunction) throws JCoException, TCException, IOException{
		String msg = "";
		JCoDestination destination = SAPConn.connect();
		JCoRepository repository = destination.getRepository();
		JCoFunction function = repository.getFunction(sapFunction);
		Collection<BOPInfoModel> list = models.values();
		List<BOPInfoModel> bomInfos = new ArrayList<>();
		bomInfos.addAll(list);
//		if (bomInfos.size() > 0) {
//			System.out.println(bomInfos.size());
//			Collections.sort(bomInfos, new Sort());
//		}
		for (BOPInfoModel bomInfo : bomInfos) {
			Map<String, Object> values = bomInfo.getModel();
			JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
			Set<String> keys = values.keySet();
			String topID = values.get("MATNR") + ":";
			String info = "";
			for (String key : keys) {
				info += key + "=" + values.get(key) + "\n";
				headTable.setValue(key, values.get(key));
			}
			System.out.println("TopBOM:" + topID + "\n" + info);
			List<BOPLineModel>  bomlineInfos = bomInfo.getBOMLinModel();
			JCoTable bomlineTable = function.getTableParameterList().getTable(input_BOM_ITEM);
			for (int i = 0; i < bomlineInfos.size(); i++) {
				BOPLineModel bomlineInfo = bomlineInfos.get(i);
				bomlineTable.insertRow(i);
				values = bomlineInfo.getModel();
				keys = values.keySet();
				String childID = values.get("IDNRK") + "";
				info = "";
				for (String key : keys) {
					info += key + "=" + values.get(key) + "\n";
					bomlineTable.setValue(key, values.get(key));
				}
				System.out.println("SubLine:" + childID + ":\n" + info);
			}
			System.out.println(bomlineTable.toString());
			function.execute(destination);
			JCoStructure table = function.getExportParameterList().getStructure(export_Table);
			String type = 	table.getString("STA");
			String message = table.getString("MESSAGE");
			String plnnr  = table.getString("PLNNR");
			String plnal = table.getString("PLNAL");	
			System.out.println(message);
			if(plnnr!=null&&plnal!=null) {
				bomInfo.setERPBackProperty("k8_PLNNR", plnnr);
				bomInfo.setERPBackProperty("k8_PLNAL", plnal);
			}
			if (!"S".equals(type)) {
				message = "SAP ERROR:"+ topID+ message;
//				log.error(message);
				msg += message + "\n";
			} else {
				bomInfo.setSentSAPFlag();
//				log.info(message);
			}
			if (msg != null && !msg.isEmpty()) {
				return msg;
			}
		}
		return msg;
	}
	
}
