package com.leoch.sie.custom.sap.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ECNModel {

	public static final String ECNSentSAPFlag = "k8_ecn_sendSAP";
	public static String AENNR = "AENNR"; // 更改编号
	public static int AENNR_L = 12; // //变更编号长度
	
	public static String AETXT = "AETXA"; // 更改编号描述
	public static int AETXT_L = 40; // 更改编号描述长度	
	
	public static String DATUV = "DATUA"; // 开始生效日期
	
	public static String AEGRU = "AEGRA"; // 更改原因
	public static int AEGRU_L = 40; // 更改原因长度
	
	public static String AENST = "AENSA"; // 更改号的状态
	public static int AENST_L = 2; // 更改号状态的长度
	
	BOMStruct struct;
	
	List<ECNItemModel> itemModels;
	
	TCComponentItem ecn;
	
	TCSession session;
	
	Map<String, Object> infos;
	
	List<TCComponentItemRevision> solu;
	
	/**
	 * ECN发送SAP的数据模型
	 *
	 * @param ecn ECN对象
	 * @param session TC会话
	 * @param solu ECN的解决方案中的物料
	 */
	    
	public ECNModel(TCComponentItem ecn, TCSession session, List<TCComponentItemRevision> solu) {
		this.ecn = ecn;
		this.session = session;
		this.solu = solu;
	}
		
	/**
	 * @Title: load
	 * @Description: 加载并检查变更和BOM信息信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return String    检查结果
	 * @throws
	 */
	    
	public String load() throws TCException {
		String msg = "";
		infos = new HashMap<>();		
		String ecnNo = ecn.getProperty("item_id"); 
		String name = ecn.getProperty("object_name");
		String reason = ecn.getProperty("k8_reason"); 	
		if (ecnNo.length() > AENNR_L) {
			msg += "ECN编码" + ecnNo + "的长度不能超过" + AENNR_L + "\n";
		}
		if (name.length() > AETXT_L) {
			msg += "更改主题" + name + "的长度不能超过" + AETXT_L + "\n";
		}
		if (reason.length() > AEGRU_L) {
			reason = reason.substring(0, 39);
//			msg += "更改原因" + reason + "的长度不能超过" + AEGRU_L + "\n";
		}
		infos.put(AENNR, ecnNo); // 变更号
		infos.put(AETXT, name); // 更改主题
		infos.put(DATUV, new Date()); // 生效日期
		infos.put(AEGRU, reason); // 变更原因
		infos.put(AENST, "01"); // 更改号状态
		itemModels = new ArrayList<>();
		for (int i = 0; i < solu.size(); i++) {
			ECNItemModel itemModel = new ECNItemModel(solu.get(i), name);
			msg += itemModel.load();
			itemModels.add(itemModel);
		}
		
		struct = new BOMStruct(solu, session, ecnNo);
		msg += struct.load();
		struct.close();
		return msg;
	}
	
	/**
	 * @Title: getModel
	 * @Description: 获取ECN对象发送SAP信息
	 * @param @return    参数
	 * @return Map<String,Object>   ECN发送SAP信息
	 * @throws
	 */
	    
	public Map<String, Object> getModel() {
		return infos;
	}
		
	/**
	 * @Title: getItemModels
	 * @Description: 获取ECN解决方案发送SAP的信息
	 * @param @return    参数
	 * @return List<ECNItemModel>    ECN解决方案发送SAP的信息
	 * @throws
	 */
	    
	public List<ECNItemModel> getItemModels(){
		return itemModels;
	}
	
	/**
	 * @Title: getBOMStruct
	 * @Description: 获取ECN下的所有BOM结构信息
	 * @param @return    参数
	 * @return BOMStruct    ECN下的所有BOM结构信息
	 * @throws
	 */
	    
	public BOMStruct getBOMStruct() {
		return struct;
	}
	
	/**
	 * @Title: setSentSAPFlag
	 * @Description: 设置ECN已发送SAP
	 * @param @throws TCException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	@SuppressWarnings("deprecation")
	public void setSentSAPFlag() throws TCException {
//		session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
		ecn.setLogicalProperty(ECNSentSAPFlag, true);
//		session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
	}
}
