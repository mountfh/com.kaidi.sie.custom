package com.leoch.sie.custom.sap.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.leoch.sie.custom.utils.Part;
import com.leoch.sie.custom.utils.NumberValidationUtils;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BOMInfoModel {
		
	public static String WERKS = "WERKS"; // 工厂(7-13取消工厂号的传递)
	
	public static String MATNR = "MATNR"; //物料编码
	
	public static int MATNR_L = 18; // 物料编码长度
	
	public static String BMENG = "BASMN"; // 基本数量
	
	public static String DATUV = "DATUV"; // 生效日期(取当前时间)
	
	public static String STLAL = "STLAL"; // 备选物料清单(BOM用途)
	
	public static String STLAN = "STLAN"; //物料清单用途(可选BOM描述 )
	
	public static String AENNR = "AENNR"; //变更编号
	
	private TCComponentBOMLine top; // 顶层BOM
	
	private List<BOMLineModel> bomlines; // BOM行信息
	
	private Map<String, Object> bomInfo; // BOM数据
	
	private TCComponentItemRevision rev;
	
	private AIFComponentContext[] subLines;
	
	private String ecnNo;

	private TCSession session;

	private String checkQuantityMsg;
	
	int level;
		
	/**
	 *
	 * @param top 顶层BOMLine
	 * @param rev 顶层BOM的版本
	 * @param encNO 变更号
	 * @param subLines BOM子项
	 * @param level2 
	 */
	    
	public BOMInfoModel(TCComponentBOMLine top, TCComponentItemRevision rev, String encNO, AIFComponentContext[] subLines, int level) {
		this.top = top;
		this.rev = rev;
		this.ecnNo = encNO;
		this.subLines = subLines;
		this.level = level;
		session = (TCSession) AIFUtility.getDefaultSession();
	}
	
	
	/**
	 * @Title: load
	 * @Description: 加载并检查BOM结构信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return String   检查结果
	 * @throws
	 */
	    
	public String load() throws TCException {
		String msg = "";
		bomInfo = new HashMap<>();
		if (top == null) {
			return msg;
		}

		String topLineId = top.getItem().getProperty("item_id");
//		msg += Part.ckeck(top.getItemRevision(), topLineId);
		if (topLineId.length() > MATNR_L) {
			msg += topLineId + "的物料编码长度不能超过" + MATNR_L + "\n";
		}
		bomInfo.put(MATNR, topLineId); // 物料号
		bomInfo.put(DATUV, new Date()); // 生效日期
		if (ecnNo != null) {
			bomInfo.put(AENNR, ecnNo);
		}
		String werks = rev.getProperty("k8_factory");
		if (werks.isEmpty()) {
			msg += topLineId + "的物料工厂 不能为空\n";
		}else  if(werks.indexOf(":") >0) {
			werks = werks.substring(0,werks.indexOf(":"));
		}
		bomInfo.put(WERKS, werks);	// 工厂
		Map<Object, Double> qs = getQuantityAndCheck(top, subLines);
		if (!checkQuantityMsg.isEmpty()) {
			msg += checkQuantityMsg;
		}
		bomInfo.put(BMENG, qs.get(top)); // 基本数量(当前默认为1)
//		bomInfo.put(CHOOSABLE_BOM, "1"); //备选物料清单
		bomInfo.put(STLAN, "1"); //用途
		
		bomlines = new ArrayList<>();
		if (subLines != null) {
			for (int i = 0; i < subLines.length; i++) {
				TCComponentBOMLine subLine = (TCComponentBOMLine) subLines[i].getComponent();
				BOMLineModel model = new BOMLineModel(subLine, topLineId, ecnNo, qs.get(subLines[i]));
				msg += model.load();
				bomlines.add(model);
			}
		}
		return msg;
		
	}
		
	/**
	 * @Title: getModel
	 * @Description: 获取BOM顶层信息
	 * @param @return    参数
	 * @return Map<String,Object>    BOM顶层信息
	 * @throws
	 */
	    
	public Map<String, Object> getModel(){
		return bomInfo;
	}
		
	/**
	 * @Title: getBOMLinModel
	 * @Description: 获取BOMLine结构
	 * @param @return    参数
	 * @return List<BOMLineModel>    BOMLine结构
	 * @throws
	 */
	    
	public List<BOMLineModel> getBOMLinModel(){
		return bomlines;
	}
		
	/**
	 * @Title: getRevision
	 * @Description: 获取BOMLine的物料版本
	 * @param @return    参数
	 * @return TCComponentItemRevision   物料版本
	 * @throws
	 */
	    
	public TCComponentItemRevision getRevision() {
		return rev;
	}
	
	
	/**
	 * @Title: setSentSAPFlag
	 * @Description: 设置BOM已发送SAP成功
	 * @param @throws TCException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	@SuppressWarnings("deprecation")
	public void setSentSAPFlag() throws TCException {
//		session.getUserService().call("avicit_call_bypass", new Object[] { 1 });
		rev.setLogicalProperty(BOMStruct.BOMSentSAPFlag, true); // 设置BOM已发送SAP
//		session.getUserService().call("avicit_call_bypass", new Object[] { 0 });
	}
		
	/**
	 * @Title: getQuantityAndCheck
	 * @Description: 转化BOM行对应数量并检查
	 * @param @param top
	 * @param @param subLines
	 * @param @return
	 * @param @throws TCException    参数
	 * @return Map<Object,Double>    BOM行对应的数量
	 * @throws
	 */
	    
	public Map<Object, Double> getQuantityAndCheck(TCComponentBOMLine top, AIFComponentContext[] subLines) throws TCException {
		double topQuantity = 10000;
		checkQuantityMsg = "";
		if (subLines != null) {
			String topID = top.getItemRevision().getProperty("item_id");
			for (int i = 0; i < subLines.length; i++) {
				TCComponentBOMLine subLine = (TCComponentBOMLine) subLines[i].getComponent();
				String subID = subLine.getItemRevision().getProperty("item_id");
				String quantity = subLine.getProperty("bl_quantity").trim();
				if (quantity.contains("/")) {
					String[] nums = quantity.split("/");
					if (nums.length == 2) {
						boolean b1 = NumberValidationUtils.isPositiveInteger(nums[0]);
						boolean b2 = NumberValidationUtils.isPositiveInteger(nums[1]);
						if (b1 && b2) {
							int q = Integer.parseInt(nums[1]);
							if (topQuantity % q != 0) {
								topQuantity = topQuantity * q;
							}
							continue;
						}
					} 
					checkQuantityMsg += topID + "的子项(" + subID + ")用量不是分数格式\n";
				} else {
					quantity =  (Double.parseDouble(quantity) * topQuantity)+"";
					boolean b = NumberValidationUtils.isQuantityNumber1(quantity);
					if (b && Double.parseDouble(quantity) == 0) {
						b = false;
					}
					if (b) {
						continue;
					}
					checkQuantityMsg += topID + "的子项(" + subID+ ")用量不是小数点前1-6位，小数点后1-7位的小数\n";
				}
			}
		}
		
		Map<Object, Double> vs = new HashMap<>();
		if (!checkQuantityMsg.isEmpty()) {
			return vs;
		}
		
		vs.put(top, topQuantity);
		if (subLines != null) {
			for (int i = 0; i < subLines.length; i++) {
				TCComponentBOMLine subLine = (TCComponentBOMLine) subLines[i].getComponent();
				String quantity = subLine.getProperty("bl_quantity").trim();
				if (quantity.contains("/")) {
					String[] nums = quantity.split("/");
					int q1 = Integer.parseInt(nums[0]);
					int q2 = Integer.parseInt(nums[1]);
					vs.put(subLines[i], topQuantity / q2 * q1);
				} else {
					double q = Double.parseDouble(quantity);
					vs.put(subLines[i], topQuantity * q);
				}
			}
		}
		return vs;		
	}	
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
}
