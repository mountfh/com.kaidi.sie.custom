package com.leoch.sie.custom.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class PreferenceUtils {

	/**
	 * @Title: getPreferenceValue
	 * @Description: TODO(获取首选项的单个值)
	 * @param @param session
	 * @param @param PreferenceName
	 * @param @return 参数
	 * @return String 返回类型
	 * @throws
	 */

	public static String getPreferenceValue(TCSession session, String preferenceName) {
		return session.getPreferenceService().getStringValue(preferenceName);
	}

	/**
	 * @Title: getPreferenceValues
	 * @Description: TODO(获取首选项多个值)
	 * @param @param session
	 * @param @param PreferenceName
	 * @param @return 参数
	 * @return String[] 返回类型
	 * @throws
	 */

	public static String[] getPreferenceValues(TCSession session, String preferenceName) {
		return session.getPreferenceService().getStringValues(preferenceName);
	}

	/**
	 * @Title: getPreferenceDescription
	 * @Description: TODO(获取首选项的描述)
	 * @param @param session
	 * @param @param PreferenceName
	 * @param @return 参数
	 * @return String 返回类型
	 * @throws
	 */

	public static String getPreferenceDescription(TCSession session, String preferenceName) {
		return session.getPreferenceService().getPreferenceDescription(preferenceName);
	}

	/**
	 * @Title: getTCComponent
	 * @Description: TODO(通过对象的UID获取对象)
	 * @param @param session
	 * @param @param uid
	 * @param @return
	 * @param @throws TCException 参数
	 * @return TCComponent 返回类型
	 * @throws
	 */

	public static TCComponent getTCComponent(TCSession session, String uid) throws TCException {
		return session.getComponentManager().getTCComponent(uid);
	}

	/**
	 * @Title: getTCComponent
	 * @Description: TODO(通过首选项配置的对象UID回去对象)
	 * @param @param PreferenceName
	 * @param @param session
	 * @param @return
	 * @param @throws TCException 参数
	 * @return TCComponent 返回类型
	 * @throws
	 */

	public static TCComponent getTCComponent(String preferenceName, TCSession session) throws TCException {
		String uid = getPreferenceValue(session, preferenceName);
		return session.getComponentManager().getTCComponent(uid);
	}

}
