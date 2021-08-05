package com.leoch.sie.custom.sap.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.leoch.sie.custom.sap.logs.ChangeBOMLog;
import com.leoch.sie.custom.sap.models.BOMInfoModel;
import com.leoch.sie.custom.sap.models.BOMLineModel;
import com.leoch.sie.custom.sap.models.BOMStruct;
import com.leoch.sie.custom.sap.models.ECNItemModel;
import com.leoch.sie.custom.sap.models.ECNModel;
import com.leoch.sie.custom.sap.models.PartModel;
import com.leoch.sie.custom.utils.SAPConn;
import com.leoch.sie.custom.utils.Sort;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

import cocom.leoch.sie.custom.oa.action.PartSyncToOAAction;

public class ECNSentToSapAction {
	
	public static String ecn_functionName = "ZPLM_ECN_INFO";
	
	public static String input_ECN_HDR = "I_ECN_INFO";
	
	public static String input_ECN_ITEM = "I_ECN_ITEM";
	
	public static String bom_functionName = "ZFUNC_005";
	
	public static String input_BOM_HDR = "I_INPUT";
		
	public static String input_BOM_ITEM = "T_TAB";
	
	public static String export_Table = "E_OUTPUT";
	
	private Logger log = ChangeBOMLog.logger;
	
	TCComponentItem ecn;
	List<TCComponentItemRevision> solus;
	TCSession session;
		    	
	public ECNSentToSapAction(TCComponentItem ecn, List<TCComponentItemRevision> solus) {
		this.ecn = ecn;
		this.solus = solus;
	}


	/**
	 * @Title: excute
	 * @Description: 执行发送SAP逻辑
	 * @param     参数
	 * @return void    返回类型
	 * @throws
	 */
		    
	public void excute() {
		session = (TCSession) AIFUtility.getDefaultSession();
		AIFDesktop desk = AIFUtility.getActiveDesktop();
		String msg = "";
		List<String> ids = new ArrayList<>();
		List<String> ids2 = new ArrayList<>();
		try {			
			List<PartModel> partModels = new ArrayList<>();
			List<PartModel> partModels2 = new ArrayList<>();
			
			for (int i = 0; i < solus.size(); i++) {
				TCComponentItemRevision part = solus.get(i);
				String revsionId = part.getProperty("item_revision_id");
				String  sentToSAP = part.getProperty(PartModel.PartSentSAPFlag);
				if (!sentToSAP.contains(revsionId)) {
					if (isDifferent(part)) {
						PartModel model2 = new PartModel(part);
						msg += model2.load();
						partModels2.add(model2);
						ids2.add(part.getProperty("item_id"));
					}
					PartModel model = new PartModel(part);
					msg += model.load();
					partModels.add(model);
					ids.add(part.getProperty("item_id"));
				}
			}
			
			if (!msg.isEmpty()) {
				MessageBox.post(desk,msg, "提示", MessageBox.INFORMATION);
				return;
			}
			//发送物料
			PartSyncToSapAction action = new PartSyncToSapAction(log);
			msg = action.sent(partModels, ids);
			if (!msg.isEmpty()) {
				MessageBox.post(desk,msg, "提示", MessageBox.INFORMATION);
				return;
			}
			//发OA
			String  oaMsg  = "";
			if (partModels2 != null && partModels2.size() != 0) {
					PartSyncToOAAction synOA = new PartSyncToOAAction();
					msg = synOA.sent(partModels2);
					if (!msg.isEmpty()) {
						MessageBox.post(desk,msg, "错误", MessageBox.ERROR);
						return;
					}
					oaMsg = ",物料新建发送SAP与OA成功！OA的流程号是："+synOA.getProcessNum();
			}
			//发ECN
			ECNModel model = new ECNModel(ecn, session, solus);
			msg = model.load();
			if (!msg.isEmpty()) {
				MessageBox.post(desk,msg, "提示", MessageBox.INFORMATION);
				return;
			}
			
			msg = sent(model);
			if (msg != null && !msg.isEmpty()) {
				MessageBox.post(desk,msg, "错误", MessageBox.ERROR);
				return;
			}
		
			MessageBox.post(desk,"ECN同步SAP成功"+oaMsg, "提示", MessageBox.INFORMATION);
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			MessageBox.post(e);
		}		
		
	}
	
	public boolean isDifferent(TCComponentItemRevision rev) throws TCException {
		TCComponentItem item = rev.getItem();
		TCComponent[] comps = item.getReferenceListProperty("revision_list");
		String oldpurch = null;// 采购类型
		String oldpush = null;// 反冲
		String purch = null;// 采购类型
		String push = null;// 反冲
		TCComponentItemRevision lastrev = null;
		if (comps.length > 1) {
			lastrev = (TCComponentItemRevision) comps[comps.length - 2];
			oldpurch = rev.getProperty("k8_procurement");
			oldpush = rev.getProperty("k8_recoil");
			purch = lastrev.getProperty("k8_procurement");
			push = lastrev.getProperty("k8_recoil");
			if (!(oldpurch.equals(purch) && oldpush.equals(push))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @Title: sent
	 * @Description: 发送SAP
	 * @param @param models 发送SAP的变更信息及需要变更的BOM信息
	 * @param @return
	 * @param @throws JCoException
	 * @param @throws TCException
	 * @param @throws IOException    参数
	 * @return String   发送SAP的结果
	 * @throws
	 */
		    
	public String sent(ECNModel model) throws JCoException, TCException, IOException{
		String msg = "";
		JCoDestination destination = SAPConn.connect();
		JCoRepository repository = destination.getRepository();
//		JCoFunction ecn_function = repository.getFunction(ecn_functionName);
		JCoFunction bom_function = repository.getFunction(bom_functionName);		
//		JCoStructure headTable = ecn_function.getImportParameterList().getStructure(input_ECN_HDR);
		
//		Map<String, Object> values = model.getModel();
//		Set<String> keys = values.keySet();
//		for (String key : keys) {
//			System.out.println("headTable:"  + key + "=" + values.get(key));
//			headTable.setValue(key, values.get(key));
//		}
//		List<ECNItemModel> itemModels = model.getItemModels();
//		JCoTable ecnTable = ecn_function.getTableParameterList().getTable(input_ECN_ITEM);
//		for (int j = 0; j < itemModels.size(); j++) {
//			ECNItemModel itemModel = itemModels.get(j);
//			ecnTable.insertRow(j);
//			values = itemModel.getModel();
//			keys = values.keySet();
//			for (String key : keys) {
//				System.out.println("ecnTable:"  + key + "=" + values.get(key));
//				ecnTable.setValue(key, values.get(key));
//			}
//		}
//		ecn_function.execute(destination);
		
//		JCoStructure tableParams = ecn_function.getExportParameterList().getStructure(export_Table);

//		String type = 	tableParams.getString("TYPE");
//		String message = tableParams.getString("MESSAGE");	
//		msg += message+ "\n";
//		if ("S".equals(type)) {
//			log.info(msg);
//		} else {
//			log.error(msg);
//			System.out.println("return Error: TYPE="  + type + ",MESSAGE=" + msg);
//			return msg;
//		}
		
		BOMStruct struct = model.getBOMStruct();
		Map<String, BOMInfoModel> bom_models = struct.getBOMInfo();
		Collection<BOMInfoModel> list = bom_models.values();
		List<BOMInfoModel> bomInfos = new ArrayList<>();
		bomInfos.addAll(list);
		if (bomInfos.size() > 1) {
			Collections.sort(bomInfos, new Sort());
		}
		for (BOMInfoModel bomInfo : bomInfos) {
//			JCoFunction bom_function = repository.getFunction(bom_functionName);	
			JCoStructure headTable = bom_function.getImportParameterList().getStructure(input_BOM_HDR);
			Map<String, Object> values = model.getModel();
			Set<String> keys = values.keySet();
			String info = "";
			for (String key : keys) {
				info += key + "=" + values.get(key) + "\n";
				headTable.setValue(key, values.get(key));
			}
			values = bomInfo.getModel();
			String topID = values.get("MATNR") + ":";
			keys = values.keySet();
			for (String key : keys) {
				info += key + "=" + values.get(key) + "\n";
				headTable.setValue(key, values.get(key));
			}
			System.out.println("TopBOM:" + topID + "\n" + info);
			List<BOMLineModel>  bomlineInfos = bomInfo.getBOMLinModel();
			JCoTable bomlineTable = bom_function.getTableParameterList().getTable(input_BOM_ITEM);
			for (int j = 0; j < bomlineInfos.size(); j++) {
				BOMLineModel bomlineInfo = bomlineInfos.get(j);
				bomlineTable.insertRow(j);
				values = bomlineInfo.getModel();
				keys = values.keySet();
				info = "";
				for (String key : keys) {
					info += key + "=" + values.get(key) + "\n";
					bomlineTable.setValue(key, values.get(key));
				}
				System.out.println("SubLine:" + values.get("IDNRK") + ":\n" + info);
			}
			bom_function.execute(destination);
			
			JCoStructure tableParams = bom_function.getExportParameterList().getStructure(export_Table);

			String type = 	tableParams.getString("STA");
			String message = tableParams.getString("MESSAGE");	
//			msg += topID +message+ "\n";
			if ("S".equals(type)) {
				bomInfo.setSentSAPFlag();
				log.info(topID + message);
			} else {
				log.error("SAP ERROR:"+ topID+message);
				return "SAP ERROR:"+ topID+message;
			}
			headTable.clear();
			bomlineTable.clear();
//			bom_function.clone();
		}
		model.setSentSAPFlag();
		return msg;
	}
	
}
