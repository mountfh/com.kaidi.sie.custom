package com.leoch.sie.custom.sap.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.leoch.sie.custom.sap.models.BOPInfoModel;
import com.leoch.sie.custom.sap.models.BOPLineModel;
import com.leoch.sie.custom.utils.MyCreateUtil;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class BOPSendUtil {
	
	public static String functionName = "ZFUNC_001"; //新建
	
	public static String functionName1 = "ZFUNC_003"; //修改
	
	public static String functionName2 = "ZFUNC_004"; //删除
	
	public static String input_BOM_HDR = "I_INPUT";
	
	public static String input_BOM_ITEM = "T_TAB";
	
	public static String export_Table = "E_OUTPUT";

	public static String getUpdateGroup(TCComponentItemRevision rev,String parentID) throws TCException{		
		 String plnnr = null;
	     String rowPartID = null;
	 	 TCComponent[] rowComps = rev.getRelatedComponents("k8_row");
		 for (int j = 0; j < rowComps.length; j++) {
			 rowPartID = rowComps[j].getProperty("k8_part");
			 if(rowPartID.equals(parentID)){
				 //变更
				 plnnr = rowComps[j].getProperty("k8_group");
			 }
		 }
		 return plnnr;		
	}
	 
	public static String deleteGySend(BOPInfoModel bomInfo,String partid,String plnnr,JCoDestination destination,JCoRepository repository) throws JCoException, TCException{
		
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
				bomInfo.removeRowProperty(rev, partid);
			}
		} 
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}
		return null;
		
	}
	
	public static String getRows(TCComponentItemRevision rev,String parentID) throws TCException{
		 
	    String rowPartID = null;
	    boolean flag = false;
		TCComponent[] rowComps = rev.getRelatedComponents("k8_row");
		for (int j = 0; j < rowComps.length; j++) {
			 rowPartID = rowComps[j].getProperty("k8_part");
			 if(rowPartID.equals(parentID)){
				 //变更
				 flag = true;
			 }
		}
		//
		if(!flag){
			return parentID;
		}else{
			return "";
		}				
	}
	
	public static void removeRow(TCComponentItemRevision comp) throws TCException{
		TCComponent[] comps = comp.getRelatedComponents("k8_row");
		if(comps.length>0){
				for (int i = 0; i < comps.length; i++) {
					comp.remove("k8_row", comps[i]);
				}
		}
	}
	
	public static String setSapGroup(TCComponentItemRevision comp,String parentID,String gcount,String group,String status) throws TCException{
		
		Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("k8_part", parentID);
		propertyMap.put("k8_group", gcount);
		propertyMap.put("k8_groupcount", group);
		propertyMap.put("k8_status", status);
		String temp = null;
		TCComponent row = MyCreateUtil.createWorkspaceObject("K8_ProcessRow", propertyMap);
				TCComponent[] comps = comp.getRelatedComponents("k8_row");
				if(comps.length>0){
					for (int i = 0; i < comps.length; i++) {
						temp = comps[i].getProperty("k8_part");
						if(temp.equals(parentID)){
							comp.remove("k8_row", comps[i]);							
						}
						comp.add("k8_row", row);
					}
				}else{
					 comp.add("k8_row", row);
				}
		return null;		
	}
	
	public static String newSent(BOPInfoModel bomInfo,String partid,JCoDestination destination,JCoRepository repository) throws JCoException, TCException{
		
		String msg = "";
		TCComponentItemRevision rev = bomInfo.getTopRev();
		JCoFunction function = repository.getFunction(functionName);
		Map<String, Object> values = bomInfo.getModel();
		JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
		Set<String> keys = values.keySet();
		values.put("MATNR", partid);
		values.put("AENNR", "");
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
	
}
