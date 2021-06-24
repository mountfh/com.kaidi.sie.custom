package com.leoch.sie.custom.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class TypeModel {
	
	String type;
	List<TCComponent> revs;
	TCSession session;
	
	String DOC_TEMP = "L8_DOC_MAIL_TEMP";
	String DESIGN_TEMP = "L8_DESIGN_MAIL_TEMP";
	String UN_SENT_MAIL_USERS = "L8_UN_SENT_MAIL_USERS";
	
	int typeRowNUM = 1;
	int startRow = 2;
	int startColumn = 0;
	private List<TCComponentUser> users;
	
	private List<TCComponentUser> unsentUsers;

	public TypeModel(String type, List<TCComponent> revs) {
		this.type = type;
		this.revs = revs;
	}
		
	public String load() throws TCException, IOException {
		String msg = "";
		if (type.isEmpty()) {
			return "大类属性不能为空！";
		}
		String rev_type = revs.get(0).getType();
		String p_name = null;
		session = (TCSession) AIFUtility.getDefaultSession();
		if ("L8_DocumentRevision".equals(rev_type)) {
			p_name = DOC_TEMP;
		} else if ("L8_DesignRevision".equals(rev_type)) {
			p_name = DESIGN_TEMP;
		}
		String uid = PreferenceUtils.getPreferenceValue(session, p_name);
		if (uid == null || uid.isEmpty()) {
			return "文档与图纸的模板首选项未配置！";
		}
		TCComponent ds = PreferenceUtils.getTCComponent(session, uid);
		if (!(ds instanceof TCComponentDataset)) {
			return p_name + "配置的UID不是数据集类型";
		}
		TCComponentDataset dataset = (TCComponentDataset) ds;
		File file = MyDataset.getFile(dataset);
		if (file == null || !file.exists()) {
			return p_name + "配置的UID找不到签收对照表文件";
		}
		String fileName = file.getName();
		if (!fileName.endsWith(".xlsx")) {
			return p_name + "配置的数据集不是.xlsx格式";
		}
		
		FileInputStream in = new FileInputStream(file);
		XSSFWorkbook book = new XSSFWorkbook(in);
		XSSFSheet sheet = book.getSheetAt(0);
		if (sheet == null) {
			return fileName + "的sheet页不能为空！";
		}
		XSSFRow r = sheet.getRow(typeRowNUM);
		int maxColumn = r.getLastCellNum();
		TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
		users = new ArrayList<>();
		unsentUsers = new ArrayList<>();
		String[] ids = PreferenceUtils.getPreferenceValues(session, UN_SENT_MAIL_USERS);
		if (ids != null && ids.length != 0) {
			for (int i = 0; i < ids.length; i++) {
				try {
					TCComponentUser user = userType.find(ids[i]);
					if (user != null) {
						unsentUsers.add(user);
					}
				} catch (TCException e) {
					System.out.println(e.toString());
				}
			}
		}
		int columnNUM = -1;
		
		for (int i = startColumn; i <= maxColumn; i++) {
			XSSFCell cell = r.getCell(i);
			if (cell == null) {
				continue;
			}
			String value = cell.getStringCellValue();
			if (type.equals(value)) {
				columnNUM = i;
				break;
			}
		}
		if (columnNUM == -1) {
			return "";
		}
		int maxROW = sheet.getLastRowNum();
		for (int i = startRow; i <= maxROW; i++) {
			XSSFRow row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			XSSFCell cell = row.getCell(columnNUM);
			if (cell == null) {
				continue;
			}
			String userID = null;
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				userID = cell.getStringCellValue();
				break;
			case Cell.CELL_TYPE_NUMERIC:
				userID = cell.getNumericCellValue() + "";
				break;
			default:
				break;
			}
			if (userID == null || userID.isEmpty()) {
				continue;
			}
			try {
				TCComponentUser user = userType.find(userID);
				if (user != null) {
					users.add(user);
				}
			} catch (TCException e) {
				System.out.println(e.toString());
			}
		}
		in.close();
		return msg;
	}
	
	public List<TCComponentUser> getReceivers() {
		return users;
	}
	
	public List<TCComponentUser> getUnReceivers() {
		return unsentUsers;
	}

}
