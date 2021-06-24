package com.leoch.sie.custom.utils;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class RuleCheck {

	public static String preference_name = "K8_SAP_Check_Rule";
		
	/**
	 * @Title: check
	 * @Description: 检查菜单功能时候是否符合流程节点要求
	 * @param @param type 菜单功能类型
	 * @param @param task 任务节点
	 * @param @return
	 * @param @throws TCException    参数
	 * @return boolean    检查结果
	 * @throws
	 */
	    
	public static boolean check(String type, TCComponentTask task) throws TCException {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		String[] rules = PreferenceUtils.getPreferenceValues(session, preference_name);
		if (rules == null || rules.length == 0) {
			throw new TCException("同步SAP首选项: "+ preference_name + "未配置!");
		}
		String nodeName = task.getName();
		String tempName = task.getRoot().getName();
		for (int i = 0; i < rules.length; i++) {
			String rule = rules[i];
			if (!rule.startsWith(type + ":")) {
				continue;
			}
			String[] infos = rule.split(":");
			if (infos.length != 2) {
				continue;
			}
			String info = infos[1];
			if (info == null || info.isEmpty()) {
				continue;
			}
			String[] values = info.split("=");
			if (values.length == 2 && nodeName.equals(values[1]) && tempName.equals(values[0])) {
				return true;
			}
		}
		return false;
	}
	
}
