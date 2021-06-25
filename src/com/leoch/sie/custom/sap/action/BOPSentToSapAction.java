package com.leoch.sie.custom.sap.action;

import java.util.List;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

public class BOPSentToSapAction {

	List<TCComponentItemRevision> revs;
	TCSession session;
	
	public static String functionName = "ZFUNC_005";
	
	
	
	/**
	 * 	     
	 * @param revs 任务目标下发送SAP的物料版本
	 */
		    
	public BOPSentToSapAction(List<TCComponentItemRevision> revs) {
		this.revs = revs;
	}
	
	public void excute() {
		
	}
}
