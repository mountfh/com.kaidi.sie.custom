package com.leoch.sie.custom.utils;

import java.util.HashMap;

public class ReleaseStatusMap {
	public static HashMap<String, String> map = null;
	
	static {
		map = new HashMap<>();
//		map.put("已发布", "TCM Released");
//		map.put("TCM 已发布", "TCM Released");
		
	    map.put("发布", "TCM Released");
	    map.put("失效", "L8_Invalid");
	    map.put("原型研制", "L8_Prototype");
	    map.put("工程试制", "L8_Engineering");
	    map.put("限用", "L8_Restrict");
	    map.put("批量", "L8_Batch");
	    map.put("暂停", "L8_Suspensive");
	}
	
	public static String getReleaseStatusName(String displayName) {
		String ret = map.get(displayName);
		if (ret == null || ret.length() == 0) {
			if (map.values().contains(displayName)) {
				return displayName;
			}
		}
		return ret;
	}
}
