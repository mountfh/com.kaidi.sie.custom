package com.leoch.sie.custom.utils;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;

public class MyPerference {

	private static final String OA_Address = "K8_OA_Address";// OA地址例：【http://192.168.1.145:88】
	private static final String Smb_Address = "K8_Smb_Address";// 两个值【Send】【Get】用:隔开
	private static final String CopyProperty = "K8_Copy_Property";
	
	private static TCPreferenceService service;

	public static String[] getSmbAddress() throws TCException {
		return getStringValues(Smb_Address);
	}

	public static String getOAAddress() throws TCException {
		return getStringValue(OA_Address);
	}

	public static String[] getCopyProperty() throws TCException{
		return getStringValues(CopyProperty);
	}
	
	public static TCPreferenceService getService() throws TCException {
		if (service == null) {
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			service = session.getPreferenceService();
			service.refresh();
		}
		return service;
	}

	public static String[] getStringValues(String perferenceName) throws TCException {

		String values[] = getService().getStringValues(perferenceName);

		return values;

	}

	public static String getStringValue(String perferenceName) throws TCException {

		String value = getService().getStringValue(perferenceName);

		return value;

	}

}