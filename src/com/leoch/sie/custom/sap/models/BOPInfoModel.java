package com.leoch.sie.custom.sap.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.leoch.sie.custom.utils.MyCreateUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BOPInfoModel {
	
	private static String MATNR = "MATNR"; //父件物料编码
	private static String WERKS = "WERKS"; //工厂
	private static String PLNNR = "PLNNR"; //组
	private static String AENNR = "AENNR"; //变更号
	private static String KTEXT = "KTEXT"; //工艺路线描述
	private static String DATUV = "DATUV"; //生效日期
	private static String VERWE = "VERWE"; //用途
	private static String STATU = "STATU"; //状态
	private static String LOSVN = "LOSVN"; //最小批量
	private static String LOSBS = "LOSBS"; //最大批量
	private static String PLNME = "PLNME"; //任务清单计量单位
	private static String PLNAL = "PLNAL"; //组计数器

	
	private TCComponentBOMLine top; // 顶层BOM
	
	private List<BOPLineModel> bomlines; // BOM行信息
	
	private Map<String, Object> bomInfo; // BOM数据
	
	private TCComponentItemRevision rev;
	
	private AIFComponentContext[] subLines;
	
	private String ecnNo;

	private TCSession session;

	public BOPInfoModel(TCComponentBOMLine topLine, TCComponentItemRevision rev, String ecnNo,
			AIFComponentContext[] subLines) {
		this.top = topLine;
		this.rev = rev;
		this.ecnNo = ecnNo;
		this.subLines = subLines;
		session = (TCSession) AIFUtility.getDefaultSession();
	}
	
	public String load() throws Exception {
		String msg = "";
		bomInfo = new HashMap<>();
		if (top == null) {
			return msg;
		}
		rev.refresh();
		String topLineId = rev.getProperty("item_id");
		String connectPartId = null;
		connectPartId = rev.getProperty("k8_MATNR2");		
//		TCComponent comp = rev.getRelatedComponent("IMAN_specification");
//		 AIFComponentContext[] comps = rev.whereReferenced();
//		for (int i = 0; i < comps.length; i++) {
//			
//			String type = comps[i].getComponent().getType();
//			if(type.contains("PartRevision")) {
//				connectPartId =  comps[i].getComponent().getProperty("item_id");
//				break;
//			}
//		}
		bomInfo.put(MATNR, connectPartId);		
		String k8_WERKS = rev.getProperty("k8_WERKS");
		bomInfo.put(WERKS, k8_WERKS);
		
		if (ecnNo != null) {
			bomInfo.put(AENNR, ecnNo);
			String k8_PLNNR = rev.getProperty("k8_PLNNR");
			bomInfo.put("PLNNR", k8_PLNNR);
			String k8_PLNAL = rev.getProperty("k8_PLNAL");
			bomInfo.put("PLNAL", k8_PLNAL);
		}				
		
		String object_desc = rev.getProperty("object_name");
		bomInfo.put(KTEXT, object_desc);
		
		String k8_DATUV = rev.getProperty("k8_DATUV");
		bomInfo.put(DATUV, k8_DATUV);
		
		String k8_VERWE = rev.getProperty("k8_VERWE");
		bomInfo.put(VERWE, k8_VERWE);
		
		String k8_STATU = rev.getProperty("k8_STAT");
		bomInfo.put(STATU, k8_STATU);
		
		String k8_LOSVN = rev.getProperty("k8LOSVN2");

		bomInfo.put(LOSVN, k8_LOSVN);
		
		String k8_LOSBS = rev.getProperty("k8_LOSBS2");

		bomInfo.put(LOSBS, k8_LOSBS);
		
		String k8_PLNME = rev.getProperty("k8_PLNME");
		bomInfo.put(PLNME, k8_PLNME);
		
		bomlines = new ArrayList<>();
		
		for (int i = 0; i < subLines.length; i++) {
			TCComponentBOMLine subLine = (TCComponentBOMLine) subLines[i].getComponent();
			BOPLineModel model = new BOPLineModel(subLine, topLineId, ecnNo);
			msg = model.load();
			bomlines.add(model);
		}		
		return msg;		
	}
	
	public TCComponentItemRevision getTopRev(){
		return rev;		
	}
	
	
	/**
	 * @param comp
	 * @param parentID
	 * @param gcount
	 * @param group
	 * @param status
	 * @throws TCException
	 */
	public void setRowProperty(TCComponentItemRevision comp,String parentID,String group,String gcount,String status) throws TCException{
	
		Map<String, String> propertyMap = new HashMap<String, String>();
		propertyMap.put("k8_part", parentID);
		propertyMap.put("k8_group", group);
		propertyMap.put("k8_groupcount", gcount);
		propertyMap.put("k8_status", status);
		TCComponent row = MyCreateUtil.createWorkspaceObject("K8_ProcessRow", propertyMap);
		comp.add("k8_row", row);				
	}
	
	/**删除gy行
	 * @param comp
	 * @param parentID
	 * @throws TCException
	 */
	public void removeRowProperty(TCComponentItemRevision comp,String parentID) throws TCException{
	 	
		String rowPartID = null;
		TCComponent[] rowComps = rev.getRelatedComponents("k8_row");
		 for (int j = 0; j < rowComps.length; j++) {
			 rowPartID = rowComps[j].getProperty("k8_part");
			 if(rowPartID.equals(parentID)){
				 comp.remove("k8_row",rowComps[j]);
			 }
		 }
	}
	
	
	public void setERPBackProperty(String property,String value) throws TCException {
//		session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
		rev.setProperty(property, value);; // 设置属性
//		session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
	}
	
	public void setSentSAPFlag() throws TCException {
//		session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
		rev.setLogicalProperty(BOMStruct.BOMSentSAPFlag, true); // 设置BOM已发送SAP
//		session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
	}
	
	public Map<String, Object> getModel(){
		return bomInfo;
	}
	
	public List<BOPLineModel> getBOMLinModel(){
		return bomlines;
	}
}
