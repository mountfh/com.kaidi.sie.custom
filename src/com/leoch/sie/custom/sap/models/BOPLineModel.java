package com.leoch.sie.custom.sap.models;

import java.util.HashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;

public class BOPLineModel {

	public static String STEUS = "STEUS";//  项控制码
	
	public static String VORNR = "VORNR";//  工序编号
	
	public static String ARBPL = "ARBPL"; // 工作中心
	
	public static String KTSCH = "KTSCH"; // 标准文本码
	
	public static String LTXA1 = "LTXA1"; // 工序名称（object_name）
	
	public static String BMSCH = "BMSCH"; // 基本数量
		
	public static String VORME = "VORME";// 计量单位
			
	public static String VGW01 = "VGW01"; // 直接人工
		
	public static String VGE01 = "VGE01"; // 直接人工单位
		
	public static String VGW02 = "VGW02"; // 间接人工
	
	public static String VGE02 = "VGE02"; // 间接人工单位
	
	public static String VGW03 = "VGW03"; // 折旧与摊销
	
	public static String VGE03 = "VGE03"; // 折旧与摊销单位

	public static String VGW04 = "VGE04"; // 能源(水电气）
	
	public static String VGE04 = "VGE04"; // 能源(水电气）单位
	
	public static String VGW05 = "VGW05"; // 维保耗材
	
	public static String VGE05 = "VGE05"; // 维保耗材单位
	
	public static String VGW06 = "VGW06"; // 其他
	
	public static String VGE06 = "VGE06"; // 其他单位	
	
	public static String CKSELKZ = "CKSELKZ"; // 成本核算相关标识
	
	public static String ANZMA = "ANZMA"; // 雇员数
	
	public static String INFNR = "INFNR"; // 采购信息记录
	
	public static String EKORG = "EKORG"; // 采购组织
	
	public static String MATKL = "MATKL"; // 物料组
	
	public static String EKGRP = "EKGRP"; // 采购组
	
	public static String SAKTO = "SAKTO"; // 成本要素
	
	private Map<String, Object> info = null;
	
	TCComponentBOMLine bomLine = null;
	
	String topLineId = null;
	
	String ecnNo = null;
	
	Double quantity = null;
//	public static String CHANGE = "CHANGE"; // 变更类型
	
	public BOPLineModel(TCComponentBOMLine bomLine, String topLineId, String ecnNO) {
		this.bomLine = bomLine;
		this.topLineId = topLineId;
		this.ecnNo = ecnNO;
//		this.quantity = quantity;
	}
	
	public String load() throws TCException {
		String msg = "";
		if (bomLine == null) {
			return msg;
		}
		info = new HashMap<>();
		
		String k8_STEUS = bomLine.getProperty("bl_occ_k8_STEUS");
		info.put(STEUS, k8_STEUS);
		
		//工序编号
		String k8_VORNR = bomLine.getProperty("bl_sequence_no");
		info.put("VORNR", k8_VORNR);
		
		String k8_ARBPL = bomLine.getProperty("bl_occ_k8_ARBPL");
		info.put("ARBPL", k8_ARBPL);
		
		String k8_KTSCH = bomLine.getProperty("bl_occ_k8_KTSCH");
		info.put("KTSCH",k8_KTSCH);
		
		String object_name = bomLine.getItemRevision().getProperty("object_name");
		info.put(LTXA1, object_name);
				
		String k8_BMSCH = bomLine.getProperty("bl_occ_k8_BMSCH");
		info.put(BMSCH, k8_BMSCH);
		
		String k8_VORME = bomLine.getProperty("bl_occ_k8_VORME");
		info.put(VORME, k8_VORME);
		
		String k8_VGW01 = bomLine.getProperty("bl_occ_k8_VGW1");
		info.put(VGW01, k8_VGW01);
		
		String k8_VGE01 = bomLine.getProperty("bl_occ_k8_VGE01");
		info.put(VGE01, k8_VGE01);
		
		String k8_VGW02 = bomLine.getProperty("bl_occ_k8_VGW2");
		info.put(VGW02, k8_VGW02);
		
		String k8_VGE02 = bomLine.getProperty("bl_occ_k8_VGE02");
		info.put(VGE02, k8_VGE02);
		
		String k8_VGW03 = bomLine.getProperty("bl_occ_k8_VGW3");
		info.put(VGW03, k8_VGW03);
		
		String k8_VGE03 = bomLine.getProperty("bl_occ_k8_VGE03");
		info.put(VGE03, k8_VGE03);
		
		String k8_VGW04 = bomLine.getProperty("bl_occ_k8_VGW4");
		info.put(VGW04, k8_VGW04);
		
		String k8_VGE04 = bomLine.getProperty("bl_occ_k8_VGE04");
		info.put(VGE04, k8_VGE04);
		
		String k8_VGW05 = bomLine.getProperty("bl_occ_k8_VGW5");
		info.put(VGW05, k8_VGW05);		
		
		String k8_VGE05 = bomLine.getProperty("bl_occ_k8_VGE05");
		info.put(VGE05, k8_VGE05);
		
		String k8_VGW06 = bomLine.getProperty("bl_occ_k8_VGW6");
		info.put(VGW06, k8_VGW06);
		
		String k8_VGE06 = bomLine.getProperty("bl_occ_k8_VGE06");
		info.put(VGE06, k8_VGE06);
				
		String k8_CKSELKZ = bomLine.getProperty("bl_occ_k8_CKSELKZ");
		info.put(CKSELKZ, k8_CKSELKZ);
		
		String k8_ANZMA = bomLine.getProperty("bl_occ_k8_ANZMA");
		info.put(ANZMA, k8_ANZMA);
		
		String k8_INFNR = bomLine.getProperty("bl_occ_k8_INFNR");
		info.put(INFNR, k8_INFNR);
		
		String k8_EKORG = bomLine.getProperty("bl_occ_k8_EKORG");
		info.put(INFNR, k8_EKORG);
		
		String k8_MATKL = bomLine.getProperty("bl_occ_k8_MATKL");
		info.put(MATKL, k8_MATKL);
		
		String k8_EKGRP = bomLine.getProperty("bl_occ_k8_EKGRP");
		info.put(EKGRP, k8_EKGRP);
		
		String k8_SAKTO = bomLine.getProperty("bl_occ_k8_SAKTO");
		info.put(SAKTO, k8_SAKTO);
		
//		info = bomLine.getProperty("");
		return msg;
		
	}
	
	
	
	public Map<String, Object> getModel() {
		return info;
	}
}
