package com.leoch.sie.custom.utils;

import java.util.Comparator;

import com.leoch.sie.custom.sap.models.BOMInfoModel;

/**
 * Ωµ–Ú≈≈–Ú
 * @author Administrator
 *
 */
public class Sort implements Comparator<Object> {

	public int compare(Object o1, Object o2) {
		BOMInfoModel t1 = (BOMInfoModel) o1;
		BOMInfoModel t2 = (BOMInfoModel) o2;
		int l1 = t1.getLevel();
		int l2 = t2.getLevel();		
		if (l1 > l2){
			return -1;
		} else if (l1 < l2){
			return 1;
		} else {
			return 0;
		}
		
	}
}
