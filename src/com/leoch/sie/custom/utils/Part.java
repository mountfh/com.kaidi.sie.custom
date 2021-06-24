package com.leoch.sie.custom.utils;

import com.leoch.sie.custom.sap.models.PartModel;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class Part {

	
	/**
	 * @Title: ckeck
	 * @Description: TODO(检查BOM结构的物料发布状态)
	 * @param @param tcc
	 * @param @param id
	 * @param @return 
	 * @param @throws TCException    参数
	 * @return String    检查结果
	 * @throws TCException
	 */
	    
	public static String ckeck(TCComponentItemRevision tcc, String id) throws TCException {		
		String status = tcc.getProperty("release_status_list");
		if (status == null || status.isEmpty()) {
			return id + "的状态不能为空\n";
		}
//		if (!id.startsWith("201") && !id.startsWith("202") && ("失效".equals(status) || "限用".equals(status))) {
//			return id + "的状态不能为:" + status + "\n";
//		}
		return "";
	}
	
	public static String ckeckBySAP(TCComponentItemRevision tcc, String id) throws TCException {		
		
		String status = tcc.getProperty(PartModel.PartSentSAPFlag);
		if (!status.equals("true")) {
			return id + "的物料没有发送过SAP。\n";
		}
		return "";
	}
	
	
	public static String[] getStatus(String id) {
		if (id.startsWith("201") || id.startsWith("202")) {
			return new String[] {"原型研制","工程试制","限用","批量","失效"};
		} else {
			return new String[] {"原型研制","工程试制","批量"};
		}
	}
	
	public static boolean isBOM(TCComponentItemRevision rev) throws TCException {
		TCComponent[] bomviews = rev.getRelatedComponents("structure_revisions");
		if (bomviews == null || bomviews.length == 0) {
			return false;
		}
		return true;
	}
}
