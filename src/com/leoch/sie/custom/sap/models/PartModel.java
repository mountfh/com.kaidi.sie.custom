package com.leoch.sie.custom.sap.models;

import java.util.HashMap;
import java.util.Map;

import com.leoch.sie.custom.utils.NumberValidationUtils;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class PartModel {
	
	public static String PartSentSAPFlag = "k8_ERP"; // 物料是否发送SAP的属性名称

//	public static String BS01     = "BS01";  //新建/修改标识    CHAR 10
//	public static String MANDT   = "MANDT";  //集团            CLNT 3
	public static String SPRAS   = "SPRAS";  //语言 LANG 1
	public static String MTART   = "MTART";  //物料类型 CHAR 4
	public static String MBRSH   = "MBRSH";  //行业领域 CHAR 1
	public static String MATNR   = "MATNR";  //物料编码 CHAR 40
	public static String MAKTX   = "MAKTX";  //物料描述 CHAR 40
	public static String MEINS   = "MEINS";  //基本计量单位 UNIT 3
	public static String MATKL   = "MATKL";  //物料组 CHAR 9
	public static String SPART   = "SPART";  //产品组 CHAR 2
	public static String BISMT   = "BISMT";  //旧物料号 CHAR 40
	public static String MSTAE   = "MSTAE";  //跨工厂物料状态 CHAR 2
	public static String BRGEW   = "BRGEW";  //毛重 QUAN 13，3
	public static String NTGEW   = "NTGEW";  //净重 QUAN 13，3
	public static String GEWEI   = "GEWEI";  //重量单位 UNIT 3
	public static String GROES   = "GROES";  //大小量纲 CHAR 32
	public static String ZEINR   = "ZEINR";  //图号 CHAR 22
	public static String NORMT   = "NORMT";  //内部订单 CHAR 18
	public static String FERTH   = "FERTH";  //模穴 CHAR 18
	public static String GRUN     = "LTEXT";  //物料长描述 CHAR 300
//	public static String ZVALUE1 = "ZVALUE1";  //预留字段1 CHAR 20
//	public static String ZVALUE2 = "ZVALUE2";  //预留字段2 CHAR 20
//	public static String ZVALUE3 = "ZVALUE3";  //预留字段3 CHAR 20
//	public static String ZVALUE4 = "ZVALUE4";  //预留字段4 CHAR 20
//	public static String ZVALUE5 = "ZVALUE5";  //预留字段5 CHAR 20
	public static String WERKS   = "WERKS"; //工厂（传SAP，多工厂用':'隔开） CHAR 4
	public static String BESKZ   = "BESKZ"; //采购类型 CHAR 1
	public static String SOBSL   = "SOBSL"; //特殊采购类 CHAR 2
	public static String RGEKZ   = "RGEKZ"; //反冲 CHAR 1
	public static String MSBOOKPARTNO = "MSBOOKPARTNO"; //产品系列
	
	public static int BS01_L  = 10;  //新建/修改标识    CHAR 10
	public static int MANDT_L = 3;  //集团            CLNT 3
	public static int SPRAS_L = 1;  //语言 LANG 1
	public static int MTART_L = 4;  //物料类型 CHAR 4
	public static int MBRSH_L = 1;  //行业领域 CHAR 1
	public static int MATNR_L = 40;  //物料编码 CHAR 40
	public static int MAKTX_L = 40;  //物料描述 CHAR 40
	public static int MEINS_L = 3;  //基本计量单位 UNIT 3
	public static int MATKL_L = 9;  //物料组 CHAR 9
	public static int SPART_L = 2;  //产品组 CHAR 2
	public static int BISMT_L = 40;  //旧物料号 CHAR 40
	public static int MSTAE_L = 2;  //跨工厂物料状态 CHAR 2
	public static int BRGEW_L = 13;  //毛重 QUAN 13，3
	public static int NTGEW_L = 13;  //净重 QUAN 13，3
	public static int GEWEI_L = 3;  //重量单位 UNIT 3
	public static int GROES_L = 32;  //大小量纲 CHAR 32
	public static int ZEINR_L = 32;  //图号 CHAR 22
	public static int NORMT_L = 18;  //内部订单 CHAR 18
	public static int FERTH_L = 18;  //模穴 CHAR 18
	public static int GRUN_L  = 300;  //物料长描述 CHAR 300
	public static int ZVALUE1_L = 20;  //预留字段1 CHAR 20
	public static int ZVALUE2_L = 20;  //预留字段2 CHAR 20
	public static int ZVALUE3_L = 20;  //预留字段3 CHAR 20
	public static int ZVALUE4_L = 20;  //预留字段4 CHAR 20
	public static int ZVALUE5_L = 20;  //预留字段5 CHAR 20
	public static int WERKS_L   = 4; //工厂（传SAP，多工厂用':'隔开） CHAR 4
	public static int BESKZ_L   = 1; //采购类型 CHAR 1
	public static int SOBSL_L   = 2; //特殊采购类 CHAR 2
	public static int RGEKZ_L   = 1; //反冲 CHAR 1
	
	private TCComponentItemRevision part;	
	
	Map<String,Object> model;
	
	TCSession session;
	
	/**
	 * 物料发送SAP数据结构封装
	 * 
	 * @param part 零部件
	 */
	    
	public PartModel(TCComponentItemRevision part) {
		this.part = part;
		session = (TCSession) AIFUtility.getDefaultSession();
	}

	/**
	 * @Title: getModel
	 * @Description: 获取物料发送SAP的信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return Map<String,Object>    返回类型
	 * @throws
	 */
	    
	public Map<String, Object> getModel() throws TCException{		
		return model;
	}
		
	/**
	 * @Title: load
	 * @Description: 加载并检查物料发送SAP的信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return String    物料的错误信息
	 * @throws
	 */
	public String load() throws TCException {
			String msg = "";
			model = new HashMap<>();
			part.refresh();
			
//			if(!(part.getTCProperty("k8_ERP").equals("true"))){
//				 model.put(BS01, "ADD"); // 新建/修改标识   
//			}else{
//			   model.put(BS01, "UPDATE"); // 新建/修改标识  
//			}
//			
			String id = part.getProperty("item_id");
			if (id.isEmpty()) {
				msg += "编码不能为空\n";
			}
			if (id.length() > MATNR_L) {
				msg += id + "的编码长度不能超过" + MATNR_L + "\n";
			}
			model.put(MATNR, id); // 物料号  
			
//			String mandt = part.getProperty("k8_group");
//			if (mandt.isEmpty()) {
//				msg += "集团不能为空\n";
//				mandt = "800";
//			}
//			if (mandt.length() > MANDT_L) {
//				msg += id + "的集团长度不能超过" + MANDT_L + "\n";
//			}
//			model.put(MANDT, mandt); //集团 
			
			String spras = part.getProperty("k8_language");
			if (spras.isEmpty()) {
//				msg += "语言不能为空\n";
				spras="1";
			}
			if (spras.length() > SPRAS_L) {
				msg += id + "的语言长度不能超过" + SPRAS_L + "\n";
			}
			model.put(SPRAS, spras); // 语言
			
			String mtart = part.getProperty("k8_material_type");
			if (mtart.isEmpty()) {
				msg += "物料类型不能为空\n";
			}
			if (mtart.length() > MTART_L) {
				msg += id + "的物料类型长度不能超过" + MTART_L + "\n";
			}
			model.put(MTART, mtart); // 物料类型
			
		  String mbrsh = part.getProperty("k8_industry_field");
			if (mbrsh.isEmpty()) {
//				msg += "行业领域不能为空\n";
				mbrsh="M";
			}
			if (mbrsh.length() > MBRSH_L) {
				msg += id + "的行业领域长度不能超过" + MBRSH_L + "\n";
			}
			model.put(MBRSH, mbrsh); // 行业领域
		
			String desc = part.getProperty("k8_description1"); 
			if (desc.isEmpty()) {
				msg += id + "的描述不能为空\n";
			}
			if (desc.length() > MAKTX_L) {
				msg += id + "的物料描述长度不能超过" + MAKTX_L + "\n";
			}
			model.put(MAKTX, desc); // 物料描述
			
			String unit = part.getProperty("k8_uom2");
			if (unit.isEmpty()) {
				msg += id + "的基本单位不能为空\n";
			}
			if (unit.length() > MEINS_L) {
				msg += id + "的基本单位长度不能超过" + MEINS_L + "\n";
			}
			model.put(MEINS, unit); // 基本单位
			
			String group = part.getProperty("k8_material_group");
			if (group.isEmpty()) {
				msg += id + "的物料组不能为空\n";
			}
			if (group.length() > MATKL_L) {
				msg += id + "的物料组长度不能超过" + MATKL_L + "\n";
			}
			model.put(MATKL, group); // 物料组
			
			String spart = part.getProperty("k8_product_team");
//			if (spart.isEmpty()) {
//				msg += id + "的产品组不能为空\n";
//			}
			if (spart.length() > SPART_L) {
				msg += id + "的产品组长度不能超过" + SPART_L + "\n";
			}
			model.put(SPART, spart); // 产品组
			
			String bismt = part.getProperty("k8_old_material_num");
			if (bismt.length() > BISMT_L) {
				msg += id + "的旧物料号长度不能超过" + BISMT_L + "\n";
			}
		    model.put(BISMT,bismt); // 旧物料号
		  
		  String status = part.getProperty("k8_life_state");
			if (status.isEmpty()) {
				msg += id + " 的物料状态不能为空\n";
			}else if(status.indexOf(":") >0)  {
				status = status.substring(0,status.indexOf(":"));
			}else if(status.contains("有效"))  {
				status = " ";
			}else {
				status = "99";
			}
			if (status.length() > MSTAE_L) {
				msg += id + " 的物料状态长度不能超过" + MSTAE_L + "\n";
			}
			model.put(MSTAE, status); // 状态
			
//			String brgew = part.getProperty("k8_rough_weight");
			String brgew = part.getDoubleProperty("k8_rough_weight")+"";
			if (brgew.equals("") && !brgew.isEmpty()) {
				if (!NumberValidationUtils.isQuantityNumber(brgew)) {
					msg += id + " 的物料毛重属性值不是三位小数的实数\n";
				} else if (Double.parseDouble(brgew) == 0){
					msg += id + " 的物料毛重属性值不能为0\n";
				}
			}
			if (brgew.length() > BRGEW_L) {
				msg += id + " 的物料毛重长度不能超过" + BRGEW_L + "\n";
			}
			model.put(BRGEW, brgew); // 毛重
			
//			String ntgew = part.getProperty("k8_net_weight");
			String ntgew = part.getDoubleProperty("k8_net_weight")+"";
			if (ntgew.equals("") && !ntgew.isEmpty()) {
				if (!NumberValidationUtils.isQuantityNumber(ntgew)) {
					msg += id + " 的物料净重属性值不是三位小数的实数\n";
				} else if (Double.parseDouble(ntgew) == 0){
					msg += id + " 的物料净重属性值不能为0\n";
				}
			}
			if (ntgew.length() > NTGEW_L) {
				msg += id + " 的物料净重长度不能超过" + NTGEW_L + "\n";
			}
			model.put(NTGEW, ntgew); // 净重
					
			String unit_weight = part.getProperty("k8_weight_unit"); 
			if(brgew.equals("0") || ntgew.equals("0")){
				if (unit_weight.isEmpty()) {
					msg += id + "的重量单位不能为空\n";
					unit_weight = "KG"; 
				}
				}
			if (unit_weight.length() > GEWEI_L) {
				msg += id + "的重量单位长度不能超过" + GEWEI_L + "\n";
			}
			model.put(GEWEI, unit_weight); // 重量单位 
			
			String groes = part.getProperty("k8_dimension"); 
//			if (groes.isEmpty()) {
//				msg += id + "的大小量纲 不能为空\n";
//			}
			if (groes.length() > GROES_L) {
				msg += id + "的大小量纲长度不能超过" + GROES_L + "\n";
			}
			model.put(GROES, groes); // 大小量纲 
			
			String zeinr = part.getProperty("k8_drawing_num");//图号
			if(zeinr == null || zeinr.isEmpty()) {
				zeinr = part.getProperty("k8_drawing_No");//历史图号
			}
			if (zeinr.length() > ZEINR_L) {
				msg += id + "的图号或者历史图号长度不能超过" + (ZEINR_L) + "\n";
			}
			model.put(ZEINR, zeinr); // 图号
			
		    
			
			String type = part.getProperty("k8_internal_order");
			if (type.length() > NORMT_L) {
				msg += id + " 的物料内部订单号长度不能超过" + NORMT_L + "\n";
			}
			model.put(NORMT, type);	// 内部订单		
			
//			String ferth = part.getProperty("k8_mold_cavity");
//			if (ferth.length() > FERTH_L) {
//				msg += id + " 的物料模穴长度不能超过" + FERTH_L + "\n";
//			}
//			model.put(FERTH, ferth);	// 模穴		
			
			String grun = part.getProperty("k8_description2");
			if (grun.isEmpty()) {
				msg += id + "的物料长描述 不能为空\n";
			}
			if (grun.length() > GRUN_L) {
				msg += id + " 的物料模穴长度不能超过" + GRUN_L + "\n";
			}
			model.put(GRUN, grun);	// 物料长描述	
			
			String werks = part.getProperty("k8_factory");
			if (werks.isEmpty()) {
				msg += id + "的物料工厂 不能为空\n";
			}else  if(werks.indexOf(":") >0) {
				werks = werks.substring(0,werks.indexOf(":"));
			}
			if (werks.length() > WERKS_L) {
				msg += id + " 的物料工厂长度不能超过" + WERKS_L + "\n";
			}
			model.put(WERKS, werks);	// 工厂（传SAP，多工厂用':'隔开）	
			
			String beskz = part.getProperty("k8_procurement");
			int beskzle = beskz.indexOf(":");
			String sobsl = part.getProperty("k8_special_procurement");
			
			if(sobsl.indexOf(":") >0) {
				sobsl = sobsl.substring(0,sobsl.indexOf(":"));
			}else if(beskzle >2) {
				sobsl =  beskz.substring(1,beskz.indexOf(":"));
			}
			if (sobsl.length() > SOBSL_L) {
				msg += id + " 的物料特殊采购类长度不能超过" + SOBSL_L + "\n";
			}
			model.put(SOBSL, sobsl);	// 特殊采购类	
			
//			if (beskz.isEmpty()) {
//				msg += id + "的物料采购类型 不能为空\n";
//			}else if(beskz.indexOf(":") >0) {
//				beskz = beskz.substring(0,beskz.indexOf(":"));
//			}
			
			if (beskz.isEmpty()) {
				msg += id + "的物料采购类型 不能为空\n";
			}else if(beskzle >0) {
				beskz = beskz.substring(0,1);
			}
			
			if (beskz.length() > BESKZ_L) {
				msg += id + " 的物料采购类型长度不能超过" + BESKZ_L + "\n";
			}
			model.put(BESKZ,beskz);	// 采购类型	
			
			
			
			
			String rgekz = part.getProperty("k8_recoil");
			if(rgekz.indexOf(":") >0) {
				rgekz = rgekz.substring(0,rgekz.indexOf(":"));
			}else {
				rgekz = " ";
			}
			if (rgekz.length() > RGEKZ_L) {
				msg += id + " 的物料反冲长度不能超过" + RGEKZ_L + "\n";
			}
			model.put(RGEKZ, rgekz);	// 反冲	
			
			return msg;
	}
		
	/**
	 * @Title: setSentSAPFlag
	 * @Description: 设置物料已发送SAP
	 * @param @throws TCException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	@SuppressWarnings("deprecation")
	public void setSentSAPFlag() throws TCException {
//		session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
		String revsionId = part.getProperty("item_revision_id");
		String flag = part.getProperty(PartSentSAPFlag);
		part.setProperty(PartSentSAPFlag, flag+revsionId);
//		session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
	}
}
