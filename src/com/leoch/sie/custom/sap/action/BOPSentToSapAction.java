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
import com.leoch.sie.custom.utils.Sort;
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
	
	public static String functionName = "ZFUNC_001";
	
//	public static String functionName = "ZFUNC_003";
	
	public static String input_BOM_HDR = "I_INPUT";
	
	public static String input_BOM_ITEM = "T_TAB";
	
	public static String export_Table = "E_OUTPUT";
	
	/**
	 * 	     
	 * @param revs 任务目标下发送SAP的物料版本
	 */
		    
	public BOPSentToSapAction(List<TCComponentItemRevision> revs) {
		this.revs = revs;
	}
	
	public void excute() {
		
		try {
			BOMStruct struct = null;
			session = (TCSession) AIFUtility.getDefaultSession();
			String msg = "";
			String parentId = "";
			for (int i = 0; i < revs.size(); i++) {
				removeRow(revs.get(i));
				TCComponent[] relatedParts = revs.get(i).getRelatedComponents("K8_Related_Part");
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
				for (int j = 0; j < relatedParts.length; j++) {
					parentId = relatedParts[j].getProperty("item_id");
					msg = sent(models,parentId,revs.get(i));

					if (msg != null && !msg.isEmpty()) {
						MessageBox.post(msg, "错误", MessageBox.ERROR);
						return;
					}
				}
				revs.get(i).setLogicalProperty(BOMStruct.BOMSentSAPFlag, true);
			}
			
			MessageBox.post("工艺表发送SAP成功", "提示", MessageBox.INFORMATION);
		} catch (Exception e) {
			e.printStackTrace();
			//log.error(e);
			MessageBox.post(e);
		}		
	}
	
	public String sent(Map<String, BOPInfoModel> models,String relatedID,TCComponentItemRevision rev) throws JCoException, TCException, IOException{
		String msg = "";
		JCoDestination destination = SAPConn.connect();
		JCoRepository repository = destination.getRepository();
		JCoFunction function = repository.getFunction(functionName);
		Collection<BOPInfoModel> list = models.values();
		List<BOPInfoModel> bomInfos = new ArrayList<>();
		bomInfos.addAll(list);
		for (BOPInfoModel bomInfo : bomInfos) {
			Map<String, Object> values = bomInfo.getModel();
			JCoStructure headTable = function.getImportParameterList().getStructure(input_BOM_HDR);
			Set<String> keys = values.keySet();
			values.put("MATNR", relatedID);
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
//				log.error(message);
				msg += message + "\n";
			} else {
				if(plnnr!=null&&!plnnr.equals("")) {
					setSapGroup(rev,topID,plnal,plnnr,"在用");
					bomInfo.setERPBackProperty("k8_PLNNR", plnnr);
					bomInfo.setERPBackProperty("k8_PLNAL", plnal);
				}
//				bomInfo.setSentSAPFlag();
//				log.info(message);
			}
			if (msg != null && !msg.isEmpty()) {
				return msg;
			}
		}
		return msg;
	}
	
	/**
	 * @param comp
	 * @param parentID
	 * @param gcount
	 * @param group
	 * @param status
	 * @throws TCException
	 */
	public void setSapGroup(TCComponentItemRevision comp,String parentID,String gcount,String group,String status) throws TCException{
	
		Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("k8_part", parentID);
		propertyMap.put("k8_group", gcount);
		propertyMap.put("k8_groupcount", group);
		propertyMap.put("k8_status", status);
		TCComponent row = MyCreateUtil.createWorkspaceObject("K8_ProcessRow", propertyMap);
		comp.add("k8_row", row);				
	}
	
	public void removeRow(TCComponentItemRevision comp) throws TCException{
		TCComponent[] comps = comp.getRelatedComponents("k8_row");
		if(comps.length>0){
				for (int i = 0; i < comps.length; i++) {
					comp.remove("k8_row", comps[i]);
				}
		}
	}
}
