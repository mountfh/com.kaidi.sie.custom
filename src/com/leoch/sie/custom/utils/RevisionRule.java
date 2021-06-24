package com.leoch.sie.custom.utils;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCSession;

public class RevisionRule {
	
	public static TCComponentRevisionRule RULE;
	
	static {
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		try {
			RULE = (TCComponentRevisionRule) session.search("³£¹æ...", new String[] { "Ãû³Æ", "ÃèÊö" },  new String[] { "Latest Rev Any Status", "Latest Rev Any Status"})[0];
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
