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
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

import cocom.leoch.sie.custom.oa.action.BOMCompareTool;
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
	private List<TCComponentItemRevision> solus2;
	TCSession session;
	
	BOMCompareTool bomc ;
	List<TCComponentBOMLine> BOMLine = new ArrayList<>();
	
	public ECNSentToSapAction(TCComponentItem ecn, List<TCComponentItemRevision> solus) {
		this.ecn = ecn;
		this.solus = solus;
	}


	/**
	 * @Title: excute
	 * @Description: ִ�з���SAP�߼�
	 * @param     ����
	 * @return void    ��������
	 * @throws
	 */
		    
	public void excute() {
		session = (TCSession) AIFUtility.getDefaultSession();
		AIFDesktop desk = AIFUtility.getActiveDesktop();
		TCComponentBOMWindowType bomWindowType;
		TCComponentBOMWindow window = null;
		String msg = "";
		List<String> ids = new ArrayList<>();
		List<String> ids2 = new ArrayList<>();
		solus2 = new ArrayList<>();
		try {			
			bomWindowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
			window = bomWindowType.create(null);
			List<PartModel> partModels = new ArrayList<>();
			List<PartModel> partModels2 = new ArrayList<>();
			
			for (int i = 0; i < solus.size(); i++) {
				TCComponentItemRevision part = solus.get(i);
				String revsionId = part.getProperty("item_revision_id");
				String  sentToSAP = part.getProperty(PartModel.PartSentSAPFlag);
				if (!sentToSAP.contains(revsionId)) {
					if (revsionId.equals("A")) {
						PartModel model2 = new PartModel(part);
						msg += model2.load();
						partModels2.add(model2);
						ids2.add(part.getProperty("item_id"));
					}
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
					
					
					TCComponentBOMLine bomLine = window.setWindowTopLine(part.getItem(), part, null, null);
					String tcbomname = bomLine.toString();
					//�Ƚ�BOM
					if (tcbomname.contains("��ͼ")) {
						BOMCompareTool(part);
					}
					
				}
			}
			
			if (!msg.isEmpty()) {
				MessageBox.post(desk,msg, "��ʾ", MessageBox.INFORMATION);
				return;
			}
			//��������
			PartSyncToSapAction action = new PartSyncToSapAction(log);
			msg = action.sent(partModels, ids);
			if (!msg.isEmpty()) {
				MessageBox.post(desk,msg, "��ʾ", MessageBox.INFORMATION);
				return;
			}
			//��OA
			String  oaMsg  = "";
			if (partModels2 != null && partModels2.size() != 0) {
					PartSyncToOAAction synOA = new PartSyncToOAAction();
					msg = synOA.sent(partModels2);
					if (!msg.isEmpty()) {
						MessageBox.post(desk,msg, "����", MessageBox.ERROR);
						return;
					}
					oaMsg = ",�����½�����SAP��OA�ɹ���OA�����̺��ǣ�"+synOA.getProcessNum();
			}
			//��ECN
			ECNModel model = new ECNModel(ecn, session, solus2);
			msg = model.load();
			if (!msg.isEmpty()) {
				MessageBox.post(desk,msg, "��ʾ", MessageBox.INFORMATION);
				return;
			}
			
			msg = sent(model);
			if (msg != null && !msg.isEmpty()) {
				MessageBox.post(desk,msg, "����", MessageBox.ERROR);
				return;
			}
		
			MessageBox.post(desk,"ECNͬ��SAP�ɹ�"+oaMsg, "��ʾ", MessageBox.INFORMATION);
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			MessageBox.post(e);
		}		
		
	}
	
	//����OAʱ�ж������Ƿ����
	public boolean isDifferent(TCComponentItemRevision rev) throws TCException {
		TCComponentItem item = rev.getItem();
		TCComponent[] comps = item.getReferenceListProperty("revision_list");
		String oldpurch = null;// �ɹ�����
		String oldpush = null;// ����
		String purch = null;// �ɹ�����
		String push = null;// ����
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
	
	public void BOMCompareTool(TCComponentItemRevision itemRev) {
		TCComponentBOMWindowType bomWindowType;
		TCComponentBOMWindow window = null;
		try {
			bomWindowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
			window = bomWindowType.create(null);
			List<TCComponentBOMLine> NewBOMLine = new ArrayList<>();
			List<TCComponentBOMLine> OldBOMLine = new ArrayList<>();
			TCComponentBOMLine bomLine = window.setWindowTopLine(itemRev.getItem(), itemRev, null, null);
			NewBOMLine = getNewBOMLine(bomLine, NewBOMLine);
			OldBOMLine = getOldBOMLine(itemRev, OldBOMLine);
			if (NewBOMLine.size() > 0 && OldBOMLine.size() > 0) {
				CompareBom(NewBOMLine, OldBOMLine, bomLine,itemRev);
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * �¾ɰ汾��bom�Ƚ�
	 * 
	 * @param NewBOMLine
	 * @param OldBOMLine
	 * @param itemRev
	 * @throws TCException ����Create = 1 �޸�Update = 2 ɾ��Delete = 3
	 */
	public void CompareBom(List<TCComponentBOMLine> NewBOMLine, List<TCComponentBOMLine> OldBOMLine,
			TCComponentBOMLine ParentLine, TCComponentItemRevision itemRev) throws TCException {
		String action = null;
		String olditemid = null;
		String newitemid = null;
		String oldquantity = null;
		String newquantity = null;
		String newSanka = null;
		String oldSanka = null;
		String newLgort = null;
		String oldLgort = null;
		Boolean flag = null;
		// ɾ����BOM��
		for (TCComponentBOMLine tcOldBOMLine : OldBOMLine) {
			tcOldBOMLine.refresh();
			olditemid = tcOldBOMLine.getProperty("bl_item_item_id");
			oldquantity = tcOldBOMLine.getProperty("bl_quantity");
			flag = true;
			for (TCComponentBOMLine tcNewBOMLine : NewBOMLine) {
				newitemid = tcNewBOMLine.getProperty("bl_item_item_id");
				newquantity = tcNewBOMLine.getProperty("bl_quantity");
				if (olditemid.equals(newitemid)) {
					flag = false;
					break;
				}
			}
			if (flag) {
				action = "3";
				if (!solus2.contains(itemRev)) {
					solus2.add(itemRev);
					System.out.println(solus2.toString());
				}
			}
		}
		// ������BOM�к��޸ĵ�BOM��
		for (TCComponentBOMLine tcNewBOMLine : NewBOMLine) {
			tcNewBOMLine.refresh();
			newitemid = tcNewBOMLine.getProperty("bl_item_item_id");
			newquantity = tcNewBOMLine.getProperty("bl_quantity");
			newSanka = tcNewBOMLine.getProperty("bl_occ_k8_Sanka");
			newLgort = tcNewBOMLine.getProperty("bl_occ_k8_Lgort");
			flag = true;
			for (TCComponentBOMLine tcOldBOMLine : OldBOMLine) {
				olditemid = tcOldBOMLine.getProperty("bl_item_item_id");
				oldquantity = tcOldBOMLine.getProperty("bl_quantity");
				oldSanka = tcOldBOMLine.getProperty("bl_occ_k8_Sanka");
				oldLgort = tcNewBOMLine.getProperty("bl_occ_k8_Lgort");
				if (olditemid.equals(newitemid)) {
					if (!oldquantity.equals(newquantity) || !newSanka.equals(oldSanka) || !newLgort.equals(oldLgort)) {
						action = "2";
						if (!solus2.contains(itemRev)) {
							solus2.add(itemRev);
							System.out.println(solus2.toString());
						}

					}
					flag = false;
					break;
				}
			}
			if (flag) {
				action = "1";
				if (!solus2.contains(itemRev)) {
					solus2.add(itemRev);
				}
			}
		}
	}
	
	public List<TCComponentBOMLine> getNewBOMLine(TCComponentBOMLine bomLine, List<TCComponentBOMLine> lines)
			throws TCException {
		AIFComponentContext[] Contexts = bomLine.getChildren();
		for (int i = 0; i < Contexts.length; i++) {
			lines.add((TCComponentBOMLine) Contexts[i].getComponent());
		}
		return lines;
	}
	
	public TCComponentItemRevision getPreviousRev(TCComponentItemRevision itemRev){
		TCComponentItemRevision rev = null;
		try {
			TCComponent[] comps = itemRev.getRelatedComponents("revision_list");
			if(comps.length>1){
				rev = (TCComponentItemRevision) comps[comps.length-2];
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
		return rev;
	}

	public List<TCComponentBOMLine> getOldBOMLine(TCComponentItemRevision itemRev, List<TCComponentBOMLine> lines)
			throws TCException {

		TCComponentBOMWindowType bomWindowType;

		bomWindowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		TCComponentBOMWindow window = bomWindowType.create(null);
		TCComponentItemRevision prerev = getPreviousRev(itemRev);
		TCComponentBOMLine bomLine = window.setWindowTopLine(prerev.getItem(), prerev, null, null);
		AIFComponentContext[] Contexts = bomLine.getChildren();
		for (int i = 0; i < Contexts.length; i++) {
			lines.add((TCComponentBOMLine) Contexts[i].getComponent());
		}

		return lines;
	}
	
	/**
	 * @Title: sent
	 * @Description: ����SAP
	 * @param @param models ����SAP�ı����Ϣ����Ҫ�����BOM��Ϣ
	 * @param @return
	 * @param @throws JCoException
	 * @param @throws TCException
	 * @param @throws IOException    ����
	 * @return String   ����SAP�Ľ��
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
