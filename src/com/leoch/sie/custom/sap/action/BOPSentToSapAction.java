package com.leoch.sie.custom.sap.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.leoch.sie.custom.sap.models.BOMStruct;
import com.leoch.sie.custom.sap.models.BOPInfoModel;
import com.leoch.sie.custom.sap.models.BOPLineModel;
import com.leoch.sie.custom.utils.MyCreateUtil;
import com.leoch.sie.custom.utils.SAPConn;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class BOPSentToSapAction {

	List<TCComponentItemRevision> revs;
	TCSession session;
	
	public static String functionName = "ZFUNC_001"; //新建
	
	public static String functionName1 = "ZFUNC_003"; //修改
	
	public static String functionName2 = "ZFUNC_004"; //删除
	
	public static String input_BOM_HDR = "I_INPUT";
	
	public static String input_BOM_ITEM = "T_TAB";
	
	public static String export_Table = "E_OUTPUT";
	
	/**
	 * 	     
	 * @param revs 任务目标下发送SAP的工艺版本
	 */
		    
	public BOPSentToSapAction(List<TCComponentItemRevision> revs) {
		this.revs = revs;
	}
	
	public void excute() {
		
		try {			
			BOMStruct struct = null;
			session = (TCSession) AIFUtility.getDefaultSession();
			String msg = "";
			//将所有A版本 的工艺表的置空。
			for (int i = 0; i < revs.size(); i++) {				
				BOPSendUtil.removeRow(revs.get(i));
			}
			//将所有工艺传到sap
				struct = new BOMStruct(revs, session); 
				msg = struct.loadBOP();
				struct.close();	
				if (!msg.isEmpty()) {
					MessageBox.post(msg, "提示", MessageBox.INFORMATION);
					return;
				}
				Map<String, BOPInfoModel> models = struct.getBOPInfo();
				if (models.size() == 0) {
					MessageBox.post("任务目标下没有需要同步SAP的工艺表！", "提示", MessageBox.INFORMATION);
					return;
				}	
//				msg = createSend(models);
				msg = send(models);
				if (msg != null && !msg.isEmpty()) {
					MessageBox.post(msg, "错误", MessageBox.ERROR);
					return;
				}			
			MessageBox.post("工艺表发送SAP成功", "提示", MessageBox.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			//log.error(e);
			MessageBox.post(e);
		}		
	}
	

	
	public String createSend(Map<String, BOPInfoModel> models) throws Exception{
		
		String msg = "";
		String relatedpartID = null;
		TCComponentItemRevision rev = null;
				
		JCoDestination destination = SAPConn.connect();
		JCoRepository repository = destination.getRepository();
		JCoFunction function = repository.getFunction(functionName);
		
		Collection<BOPInfoModel> list = models.values();
		List<BOPInfoModel> bomInfos = new ArrayList<>();
		bomInfos.addAll(list);
		for (BOPInfoModel bomInfo : bomInfos) {
			 rev= bomInfo.getTopRev();
			 TCComponent[] relatedParts = rev.getRelatedComponents("K8_Related_Part");
			 Map<String, Object> values = bomInfo.getModel();
			 for (int j = 0; j < relatedParts.length; j++) {
					String group = null;
					JCoTable bomlineTable = function.getTableParameterList().getTable(input_BOM_ITEM);
				    relatedpartID =relatedParts[j].getProperty("item_id");
					JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
					Set<String> keys = values.keySet();
					values.put("MATNR", relatedpartID);					
					relatedpartID = (String) values.get("MATNR");
					group = BOPSendUtil.getUpdateGroup(rev,relatedpartID);
					if(group!=null){
						values.put("PLNAL", "1");
						values.put("PLNNR", group);	
					}
					String info = "";
					for (String key : keys) {
						info += key + "=" + values.get(key) + "\n";
						headTable.setValue(key, values.get(key));
					}
					System.out.println("TopBOM:" + relatedpartID + ":"+ "\n" + info);
					List<BOPLineModel>  bomlineInfos = bomInfo.getBOMLinModel();					
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
					if (!"S".equals(type)) {
						message = "SAP ERROR:物料"+ relatedpartID+ message;
//						log.error(message);
						msg += message + "\n";
					} else {
						if(plnnr!=null&&!plnnr.equals("")) {
							bomInfo.setRowProperty(rev, relatedpartID,plnnr, plnal, "在用");
						}
						bomInfo.setSentSAPFlag();
//						log.info(message);
					}
					if (msg != null && !msg.isEmpty()) {
						return msg;
					}				
			}
		}
		return msg;
	}
	
	public String send(Map<String, BOPInfoModel> models) throws Exception{
		
		JCoDestination destination = SAPConn.connect();
		JCoRepository repository = destination.getRepository();
		String msg = "";
		List<BOPInfoModel> bomInfos = new ArrayList<>();
		Collection<BOPInfoModel> list = models.values();
		bomInfos.addAll(list);
		TCComponentItemRevision rev = null;
		String group = null;
		String partid = null;
		for (BOPInfoModel bomInfo : bomInfos) {
			 rev= bomInfo.getTopRev();
			 rev.refresh();
			 TCComponent[] relatedParts = rev.getRelatedComponents("K8_Related_Part");
			 for (int i = 0; i < relatedParts.length; i++) {
				 partid = relatedParts[i].getProperty("item_id");
				 group = BOPSendUtil.getUpdateGroup(rev,partid);
				 if(group!=null){
					 //存在组号先删除
					 msg = deleteGySend(bomInfo,partid,group,destination,repository);
					 if(!msg.equals("")){throw  new Exception(msg);}
				 }else{
					 //不存在组号，新建工艺
					 msg = newSent(bomInfo,partid,destination,repository);
					 if(!msg.equals("")){throw  new Exception(msg);}
				 }
			}
			 
		}	
		return msg;
	}
	
	public String deleteGySend(BOPInfoModel bomInfo,String partid,String plnnr,JCoDestination destination,JCoRepository repository) throws JCoException{
		
		String msg = "";
		TCComponentItemRevision rev = bomInfo.getTopRev();
		JCoFunction function = repository.getFunction(functionName2);
		Map<String, Object> values = bomInfo.getModel();
		JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
		Set<String> keys = values.keySet();
		values.put("MATNR", partid);
		values.put("PLNNR", plnnr);
		values.put("PLNAL", "1");
		String topID = (String) values.get("MATNR");
		String info = "";
		for (String key : keys) {
			info += key + "=" + values.get(key) + "\n";
			headTable.setValue(key, values.get(key));
		}
		System.out.println("TopBOM:" + topID + ":"+ "\n" + info);
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
		String type = table.getString("STA");
		String message = table.getString("MESSAGE");
		plnnr  = table.getString("PLNNR");
		String plnal = table.getString("PLNAL");	
		System.out.println(message);
		if (!"S".equals(type)) {
			message = "SAP ERROR:"+ topID+ message;
//			log.error(message);
			msg += message + "\n";
		}else{
			if(!plnnr.equals("")||!plnal.equals("")){
//				bomInfo.setRowProperty(rev, partid,plnnr, plnal, "在用");
			}
		} 
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}
		return null;
		
	}
	
	public String newSent(BOPInfoModel bomInfo,String partid,JCoDestination destination,JCoRepository repository) throws JCoException, TCException{
		
		String msg = "";
		TCComponentItemRevision rev = bomInfo.getTopRev();
		JCoFunction function = repository.getFunction(functionName);
		Map<String, Object> values = bomInfo.getModel();
		JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
		Set<String> keys = values.keySet();
		values.put("MATNR", partid);
		String topID = (String) values.get("MATNR");
		String info = "";
		for (String key : keys) {
			info += key + "=" + values.get(key) + "\n";
			headTable.setValue(key, values.get(key));
		}
		System.out.println("TopBOM:" + topID + ":"+ "\n" + info);
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
		if (!"S".equals(type)) {
			message = "SAP ERROR:"+ topID+ message;
//			log.error(message);
			msg += message + "\n";
		}else{
			if(!plnnr.equals("")||!plnal.equals("")){
				bomInfo.setRowProperty(rev, partid,plnnr, plnal, "在用");
			}
		}

		headTable.clear();
		bomlineTable.clear();
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}
		return msg;
	}

	
	public String changeSent(BOPInfoModel bomInfo,String partid,String plnnr,JCoDestination destination,JCoRepository repository) throws JCoException, TCException{
		
		JCoFunction function = repository.getFunction(functionName1);
		String msg = "";
		TCComponentItemRevision rev = bomInfo.getTopRev();
		Map<String, Object> values = bomInfo.getModel();
		JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
		Set<String> keys = values.keySet();
		values.put("MATNR", partid);
		values.put("PLNNR", plnnr);
		values.put("PLNAL", "1");
		String topID = (String) values.get("MATNR");
		String info = "";
		for (String key : keys) {
			info += key + "=" + values.get(key) + "\n";
			headTable.setValue(key, values.get(key));
		}
		System.out.println("TopBOM:" + topID + ":"+ "\n" + info);
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
		plnnr  = table.getString("PLNNR");
		String plnal = table.getString("PLNAL");	
		System.out.println(message);
		if (!"S".equals(type)) {
			message = "SAP ERROR:"+ topID+ message;
//			log.error(message);
			msg += message + "\n";
		}else{
			if(!plnnr.equals("")||!plnal.equals("")){
				System.out.println(plnnr+":>"+plnal);
//				bomInfo.setRowProperty(rev, partid,plnnr, plnal, "在用");
			}
		} 
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}
		return msg;
		
	}
	
}
