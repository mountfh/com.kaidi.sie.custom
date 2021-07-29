package com.leoch.sie.custom.sap.models;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.leoch.sie.custom.utils.Part;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class BOMLineModel {

	TCComponentBOMLine bomLine = null;
	
	public static String POSTP = "POSTP";//  项目类别(L) 
	
	public static String POSNR = "POSNR"; // BOM项目号(非必填)
	public static int POSNR_L = 4;

	public static String IDNRK = "IDNRK";// BOM组件(子件物料编码)
	public static int IDNRK_L = 18; // BOM组件长度
	
	public static String MENGE = "MENGE"; // 组件数量
		
	public static String MEINS = "MEINS"; // 组件计量单位(必填)
	public static int MEINS_L = 3; // 组件计量单位长度
	
	public static String AUSCH = "AUSCH"; // 部件废品率(非必填)(子件损耗率)
	public static int AUSCH_L = 5; 
	
	public static String SANKA = "SANKA"; // 与成本相关(默认为X，虚拟件/客供料不填，由工程师选填；Z004不填)
	public static int SANKA_L = 1; 
	
	public static String LGORT = "LGORT"; // 非必填（反冲物必填，一个车间一个值）
	public static int LGORT_L = 4; 
	
	public static String FMENG = "FMENG"; // 数量固定(非必填)
	
	public static String POTX1 = "POTX1"; // BOM 项目文本(非必填)
	public static int POTX1_L = 40; // BOM 项目文本长度
	
	public static String ALPGR = "ALPGR"; // 替代项目：组(非必填)
	
	public static String ALPRF = "ALPRF"; // 替代项目：评比定单(非必填)
	
	public static String ALPST = "ALPST"; // 替代项目：策略(非必填)
	
	public static String EWAHR = "EWAHR"; // 使用可能性按 % (BTCI)(非必填)
	
//	public static String NUMBER = "NUMBER"; // 查找编号
	
	private Map<String, Object> info = null;
	
	String topLineId = null;
	
	String ecnNo = null;
	
	Double quantity = null;
			
	/**
	 *
	 * @param bomLine BOMLine对象
	 * @param topLineId 父项ID
	 * @param ecnNO 变更号
	 * @param quantity 数量
	 */
	    
	public BOMLineModel(TCComponentBOMLine bomLine, String topLineId, String ecnNO, Double quantity) {
		this.bomLine = bomLine;
		this.topLineId = topLineId;
		this.ecnNo = ecnNO;
		this.quantity = quantity;
	}
		
	/**
	 * @Title: load
	 * @Description: 加载并检查BOMLine信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return String    错误信息
	 * @throws
	 */
	    
	public String load() throws TCException {
		String msg = "";
		if (bomLine == null) {
			return msg;
		}
		info = new HashMap<>();
		TCComponentItemRevision rev = bomLine.getItemRevision();
		String bomLineId = rev.getProperty("item_id");
		
		msg += Part.ckeckBySAP(rev, bomLineId);
		
//		String isSentSAP = rev.getProperty(PartModel.PartSentSAPFlag);
//		if (!isSentSAP.equals("true")) {
//			msg += bomLineId + "物料未同步SAP\n";
//		}
		info.put(POSTP, "L"); // 项目类别（物料单）,默认值为L
		if (bomLineId.length() > IDNRK_L) {
			msg += bomLineId + "物料编码长度不能超过" + IDNRK_L + "\n";
		}
		info.put(IDNRK, bomLineId); // 物料号
		String symbol = bomLine.getProperty("K8_Symbol");
		if(symbol.isEmpty() || symbol.equals("+")) {
			info.put(MENGE, quantity); // 组件数量
		}else {
			info.put(MENGE, "-"+quantity); // 组件数量
		}
		String posnr = bomLine.getProperty("bl_sequence_no"); // BOM行号
		if (posnr.length() > POSNR_L) {
			msg += bomLineId + "BOM行号长度不能超过" + POSNR_L + "\n";
		}
		info.put(POSNR, posnr);
		
		String unit = bomLine.getProperty("bl_K8_PartRevision_k8_uom2"); // BOM工程单位
		if (unit.isEmpty()) {
			msg += bomLineId + "工程单位不能为空\n";
		} else if (unit.length() > MEINS_L) {
			msg += bomLineId + "工程单位长度不能超过" + MEINS_L + "\n";
		}
		info.put(MEINS, unit);
		
		String ausch = bomLine.getProperty("bl_occ_k8_Sub_component"); // 子件损耗率
		if (ausch.length() > AUSCH_L) {
			msg += bomLineId + "子件损耗率长度不能超过" + AUSCH_L + "\n";
		}
		info.put(AUSCH, ausch);
		
		//判断查找编号是否正确
		String number = bomLine.getProperty("bl_sequence_no"); // 查找编号
		String numberlen = number.substring(number.length() -1,number.length());
		if (!numberlen.equals("0")) {
			System.out.println(numberlen);
			msg += bomLineId + ":中的查找编号属性最后一位需为：0" + "\n";
		}

		String sanka = bomLine.getProperty("bl_occ_k8_Sanka"); // 与成本相关
//		if(sanka.isEmpty() || sanka.trim().equals("")) {
//			sanka = "X";
//		}
		if (sanka.length() > SANKA_L) {
			msg += bomLineId + "与成本相关长度不能超过" + SANKA_L + "\n";
		}
		info.put(SANKA, sanka);
	
		String lgort = bomLine.getProperty("bl_occ_k8_Lgort"); // 投料库存地点
		String lgort_v= null;
		Pattern pattern = Pattern.compile("[0-9]*");
		if (!lgort.isEmpty()) {
			lgort_v = lgort.substring(0, 4);
			if (!pattern.matcher(lgort_v).matches()) {
				msg += bomLineId + "投料库存地点填写出错，请删除数字以外的值" + "\n";
			}
			if (lgort_v.length() > LGORT_L) {
				msg += bomLineId + "投料库存地点长度不能超过" + LGORT_L + "\n";
			}
			info.put(LGORT, lgort_v);
		}else {
			info.put(LGORT, lgort);
		}
		
//		String note = bomLine.getProperty("L8_note"); // BOM备注
//		if (note.length() > POTX1_L) {
//			msg += bomLineId + "BOM备注长度不能超过" + POTX1_L + "\n";
//		}
//		info.put(POTX1, note);
		return msg;
	}
		
	/**
	 * @Title: getModel
	 * @Description: 获取BOMLine信息
	 * @param @return    参数
	 * @return Map<String,Object>    BOMLine信息
	 * @throws
	 */
	    
	public Map<String, Object> getModel() {
		return info;
	}
}
