package com.leoch.sie.custom.sap.models;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.leoch.sie.custom.utils.Part;
import com.leoch.sie.custom.utils.RevisionRule;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class BOMStruct {
	
	private List<TCComponentItemRevision> revs;
	
	private TCSession session;	
	
	private TCComponentBOMWindow window;
	
	Map<String, BOMInfoModel> info;
	
	Map<String,BOPInfoModel> BOPinfo;
	
	String msg;
	
	private String ecnNo = null;
	
	public static String BOMSentSAPFlag = "k8_BOM_SendSAP";
	
	public BOMStruct(List<TCComponentItemRevision> revs, TCSession session) {
		this.revs = revs;
		this.session = session;
	}
		
	/**
	 * BOM结构
	 *
	 * @param revs 零组件版本
	 * @param session TC会话
	 * @param ecnNo 变更号
	 */
	    
	public BOMStruct(List<TCComponentItemRevision> revs, TCSession session, String ecnNo) {
		this.revs = revs;
		this.session = session;
		this.ecnNo = ecnNo;
	}
	
	/**
	 * @Title: load
	 * @Description: 加载并检查BOM信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return String    BOM检查结果
	 * @throws
	 */
	    
	public String load() throws TCException {
		TCComponentRevisionRule rule = RevisionRule.RULE;
		if (rule == null) {
			throw new TCException("获取版本规则(Latest Rev Any Status)失败,无法解析BOM！");
		}
		TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		window = type.create(rule);
		msg = "";
		info = new LinkedHashMap<>();
		for (int i = 0; i < revs.size(); i++) {
			TCComponentItemRevision rev = revs.get(i);
			boolean isSentSap = rev.getLogicalProperty(BOMSentSAPFlag);
			if (ecnNo == null  && isSentSap) {
				continue;
			}
			TCComponentItem item = rev.getItem();
			TCComponentBOMLine topLine = window.setWindowTopLine(item, rev, null, null);
			if (topLine == null) {
				throw new TCException(rev.toString() + " 发送结构管理器失败");
			}
			loadModel(topLine, 0);
		}		
		return msg;		
	}
	
	public String loadBOP() throws Exception{
		
		TCComponentRevisionRule rule = RevisionRule.RULE;
		if (rule == null) {
			throw new TCException("获取版本规则(Latest Rev Any Status)失败,无法解析BOM！");
		}
		TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		window = type.create(rule);
		msg = "";
		BOPinfo = new LinkedHashMap<String, BOPInfoModel>();
		for (int i = 0; i < revs.size(); i++) {
			TCComponentItemRevision rev = revs.get(i);
			boolean isSentSap = rev.getLogicalProperty(BOMSentSAPFlag);
			if (ecnNo == null  && isSentSap) {
				continue;
			}
			TCComponentItem item = rev.getItem();
			TCComponentBOMLine topLine = window.setWindowTopLine(item, rev, null, null);
			if (topLine == null) {
				throw new TCException(rev.toString() + " 发送结构管理器失败");
			}
			loadBOPModel(topLine, 0);
		}		
		return msg;
	}
	
	/**加载所有工艺表信息
	 * @param topLine
	 * @param level
	 * @throws Exception 
	 */
	public void loadBOPModel(TCComponentBOMLine topLine, int level) throws Exception {
		AIFComponentContext[] subLines = unpackBOMLine(topLine);
		TCComponentItemRevision rev = topLine.getItemRevision();
		boolean isSentSap = rev.getLogicalProperty(BOMSentSAPFlag);
		if(isSentSap) {}
		String id = rev.getProperty("item_id");
		if (subLines != null && subLines.length > 0) {
//			BOPInfoModel model = new BOPInfoModel();
			if (ecnNo != null && !isSentSap) { //是否变更
				BOPInfoModel model = new BOPInfoModel(topLine, rev, ecnNo, subLines);
				msg += model.load();
				BOPinfo.put(id, model);
			}else if(ecnNo == null && !isSentSap){
				BOPInfoModel model = new BOPInfoModel(topLine, rev, ecnNo, subLines);
				msg += model.load();
				BOPinfo.put(id, model);
			}else {
				BOPInfoModel model = new BOPInfoModel(topLine, rev, ecnNo, subLines);
				msg += model.load();
				BOPinfo.put(id, model);
			}
		}
	}
		
	/**
	 * @Title: loadModel
	 * @Description: 加载所有BOM层级的信息
	 * @param @param topLine 顶层BOMLine
	 * @param @param level BOM层级
	 * @param @throws TCException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	public void loadModel(TCComponentBOMLine topLine, int level) throws TCException {
		AIFComponentContext[] subLines = unpackBOMLine(topLine);
		TCComponentItemRevision rev = topLine.getItemRevision();
		boolean isSentSap = rev.getLogicalProperty(BOMSentSAPFlag);
		String id = rev.getProperty("item_id");
		if (subLines != null && subLines.length > 0) {
			if (info.get(id) == null) { // 重复BOM结构不加载
				if (ecnNo != null ||  !isSentSap) { // 已存在SAP的BOM不重复发送
					BOMInfoModel model = new BOMInfoModel(topLine, rev, ecnNo, subLines, level);
					msg += model.load();
					info.put(id, model);
				}
			} else {
				BOMInfoModel model = info.get(id);
				int l = model.getLevel();
				if (level > l) {
					model.setLevel(l);
				}
			}
			if(ecnNo == null) {
				for (int i = 0; i < subLines.length; i++) {
					TCComponentBOMLine subLine = (TCComponentBOMLine) subLines[i].getComponent();
					loadModel(subLine, level + 1);
				}
			}
		} else {
			boolean flag = Part.isBOM(rev);
			if (ecnNo != null && flag && level == 0) {
				// 变更同步SAP时,目标下的BOM子件为空的BOM视图时,发送空BOM结构到SAP
				if (info.get(id) == null) { 
					if (!isSentSap) { 
						BOMInfoModel model = new BOMInfoModel(topLine, rev, ecnNo, subLines, level);
						msg += model.load();
						info.put(id, model);
					}
				}
			} else if (ecnNo == null && level == 0) {
			//  BOM发送SAP时，目标下的BOM没有子项时，设置BOM同步SAP标识
				if (info.get(id) == null) { 
					if (!isSentSap) { 
						BOMInfoModel model = new BOMInfoModel(topLine, rev, ecnNo, subLines, level);
//						msg += model.load();
//						info.put(id, model);
						model.setSentSAPFlag();
					}
				}
			}
		}		
	}
	
	/**
	 * @Title: unpackBOMLine
	 * @Description: 解包BOMLine
	 * @param @param topLine 顶层BOMLine
	 * @param @return
	 * @param @throws TCException    参数
	 * @return AIFComponentContext[]   解包状态下的BOMLine子项
	 * @throws
	 */
	    
	public static AIFComponentContext[] unpackBOMLine(TCComponentBOMLine topLine) throws TCException {
		if (topLine == null) {
			return null;
		}
		AIFComponentContext[] subLines = topLine.getChildren();
		if (subLines == null) {
			return null;
		}
		for (int i = 0; i < subLines.length; i++) {
			TCComponentBOMLine bomLines = (TCComponentBOMLine) subLines[i].getComponent();
			if (bomLines.isPacked()) {
				bomLines.unpack();
			}
		}
		return topLine.getChildren();
	}
		
	/**
	 * @Title: getBOMInfo
	 * @Description: 获取BOM发送SAP的信息
	 * @param @return    参数
	 * @return Map<String,BOMInfoModel>    返回类型
	 * @throws
	 */
	    
	public Map<String, BOMInfoModel> getBOMInfo(){
		return info;
	}
	
	public Map<String, BOPInfoModel> getBOPInfo(){
		return BOPinfo;
	}
		
	/**
	 * @Title: close
	 * @Description: 关闭BOM窗口，释放资源
	 * @param @throws TCException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	public void close() throws TCException {
		if (window != null) {
			window.close();
		}
	}
	
}
