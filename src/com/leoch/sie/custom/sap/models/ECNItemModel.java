package com.leoch.sie.custom.sap.models;

import java.util.HashMap;
import java.util.Map;

import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

public class ECNItemModel {

	public static String MATNR = "MATNR"; // 物料编号
	public static int MATNR_L = 12; // 物料编码长度
	
	public static String OITXT = "OITXT"; // 描述
	public static int OITXT_L = 40; // 描述长度
	
	private TCComponentItemRevision rev;
	
	private String desc;
	
	Map<String, Object> infos;	
	
	/**
	 *
	 * @param rev 解决方案下的物料版本
	 * @param desc 更改主题作为零部件的描述
	 */
	    
	public ECNItemModel (TCComponentItemRevision rev, String desc) {
		this.rev =rev;
		this.desc = desc;
	}
		
	/**
	 * @Title: load
	 * @Description: 加载并检查ECN解决方案信息
	 * @param @return
	 * @param @throws TCException    参数
	 * @return String    错误信息
	 * @throws
	 */
	    
	public String load() throws TCException {
		String msg = "";
		if (rev == null) {
			return msg;
		}
		infos = new HashMap<>();
		String id = rev.getProperty("item_id");
		if (id.length() > MATNR_L) {
			msg += "解决方案零组件物料编码 "+ id + "长度不能超过" + MATNR_L + "\n";
		}
		if (desc.length() > OITXT_L) {
			msg += "解决方案零组件物料描述 "+ desc + "长度不能超过" + OITXT_L + "\n";
		}
		infos.put(MATNR, id); // 物料号
		infos.put(OITXT, desc); // 描述
		return msg;
	}
		
	/**
	 * @Title: getModel
	 * @Description: 获取解决方案零件信息
	 * @param @return    参数
	 * @return Map<String,Object>   解决方案零件信息
	 * @throws
	 */
	    
	public Map<String, Object> getModel(){
		return infos;
	}
}
