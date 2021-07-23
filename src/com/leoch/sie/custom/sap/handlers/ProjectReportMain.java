package com.leoch.sie.custom.sap.handlers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.leoch.sie.custom.utils.SAPConn;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentSchedule;
import com.teamcenter.rac.kernel.TCComponentScheduleTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class ProjectReportMain {
	
	
	private TCComponent[] projects;
	private List<LinkedHashMap<String, String>> projectValueList;
	private LinkedHashMap<String, String> projectValueMap;
	private int count = 0;
	private String path = null;
	private String[] propetys = new String[] {"企画方针","商品化决定","出图","制造试作","仕样书","认证申请","认证取得","量产"};
	
	public  ProjectReportMain(Date date1,Date date2,String path) throws Exception {
		this.path = path;
		TCSession session = (TCSession) AIFUtility.getDefaultSession();	
		session.setStatus("后台正在导出...");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd HH:mm");
		String aftertime = sdf.format(date1);
		String beforetime = sdf.format(date2);
		projects = session.search("Search Project", new String[] { "Before" ,"After"}, new String[] {beforetime,aftertime});
	}
	
	public String excute() throws Exception {
		count = 0;
		projectValueList = new ArrayList<LinkedHashMap<String, String>>(); 
		if(projects.length>0) {
			for (int i = 0; i < projects.length; i++) {
				TCComponent tcComponent = projects[i];
				getProjectValue(tcComponent);				
			}
			wirteExcel();
			return null;
		}else {
			return "该时间段没有查询到项目！";
		}
				
	}
	
	private void wirteExcel() throws IOException {
		
		SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
		String datehm = sdf.format(new Date());
		
		LinkedHashMap<String, String> valueMap = new LinkedHashMap<String, String>();
		InputStream is = this.getClass().getResourceAsStream("/resources/松下项目报表模板.xlsx");
		if(is==null) {
			throw new IOException("找不到模板文件，无法导出");
		}
		XSSFWorkbook wb = new XSSFWorkbook(is);	
		XSSFSheet sheet = wb.getSheetAt(0);
		String name = sheet.getSheetName();
		System.out.println(name);
		for (int i = 0; i < projectValueList.size(); i++) {
			valueMap = projectValueList.get(i);
			XSSFRow row = sheet.getRow(i+4);
			if(row==null) {
				row = sheet.createRow(i+4);
			}
			int column = 0;
			for(String value:valueMap.values()) {
				XSSFCell cell = row.getCell(column);
				if (cell == null) {
					cell = row.createCell(column);
				}
				cell.setCellValue(value);
				column = column+1;
			}
		}
		setAutoWidth(sheet,27);
		sheet.setColumnWidth(0, 1000);
//		String path = System.getProperty("user.home");
		FileOutputStream fileOut = new FileOutputStream(path+"\\"+datehm+"项目报表.xlsx");
		wb.write(fileOut);
		fileOut.close();
		File file = new File(path+"\\"+datehm+"项目报表.xlsx");
		Desktop.getDesktop().open(file);

	}

	public void getProjectValue(TCComponent project) throws Exception {
		count = count+1;

		projectValueMap = new LinkedHashMap<String, String>();
		
		projectValueMap.put("序号", count+"");
		
		String name  = project.getProperty("object_name");
		projectValueMap.put("项目名称", name);
		
		String project_id = project.getProperty("project_id");
		projectValueMap.put("项目ID", project_id);
		
		String project_desc = project.getProperty("project_desc");
		projectValueMap.put("项目描述", project_desc);
		
		String production_num = project.getProperty("p8_production_num");
		projectValueMap.put("机种数", production_num);
		
		String development_level = project.getProperty("p8_development_level");
		projectValueMap.put("开发等级", development_level);
		
		String market = project.getProperty("p8_market");
		projectValueMap.put("市场", market);
		
		String plan_quantity = project.getProperty("p8_plan_quantity");
		projectValueMap.put("企画台数", plan_quantity);
		
		String plan_user = project.getProperty("p8_plan_user");
		projectValueMap.put("企画担当", plan_user);
		
		String design_user = project.getProperty("p8_design_user");
		projectValueMap.put("设计担当", design_user);

		String production_base = project.getProperty("p8_production_base");
		projectValueMap.put("生产据点", production_base);

		TCComponent schedule = getSchedule(project);
		if(schedule!=null) {
			getScheduleValue(schedule);
		}		
		projectValueList.add(projectValueMap);
		
	}
	
	public TCComponent getSchedule(TCComponent project) throws TCException {
		
		 project.refresh();
		 TCComponentProject project1 = (TCComponentProject) project;
		 TCComponent[] comps = project1.getRelatedComponents();	
		 TCComponent scheduleTask = null;
		 for (int i = 0; i < comps.length; i++) {
			 if(comps[i] instanceof TCComponentSchedule) {
				String baseschedulename = comps[i].getProperty("based_on");
				if(baseschedulename.contains("成品")) {
					TCComponentSchedule schedule= (TCComponentSchedule)comps[i];
					scheduleTask = schedule.getRelatedComponent("fnd0SummaryTask");		
				}
			 }else if(comps[i] instanceof TCComponentScheduleTask){
				 TCComponent temp = comps[i].getRelatedComponent("schedule_tag");
				 String baseschedulename = temp.getProperty("based_on");
				 if(baseschedulename.contains("成品")) {
					scheduleTask = comps[i];
				}
			 }
		}
		return scheduleTask;
	}
	
	public void getScheduleValue(TCComponent scheduleTask) throws Exception {
		
		String temp = null;
		String start_date = null;
		String finish_date = null;
		AIFComponentContext[] childs = scheduleTask.getChildren();
		TCComponent comp = null;
		List<String> resultList= new ArrayList<>(Arrays.asList(propetys));	
		HashMap<String, TCComponent> childsMap = new HashMap<String, TCComponent>();
		for (int i = 0; i< childs.length; i++) {
			comp = (TCComponent) childs[i].getComponent();
			temp =comp.getProperty("object_name");
			if(resultList.contains(temp)) {
				childsMap.put(temp, comp);
			}
		}
		if(childsMap.size()>0) {
			for (int i = 0; i < resultList.size(); i++) {
				comp = childsMap.get(resultList.get(i));
				if(comp!=null) {
					start_date = comp.getProperty("start_date");
					finish_date = comp.getProperty("actual_finish_date");
					projectValueMap.put(resultList.get(i)+"开始时间",start_date);
					projectValueMap.put(resultList.get(i)+"结束时间",finish_date);
				}else {
					projectValueMap.put(resultList.get(i)+"开始时间","");
					projectValueMap.put(resultList.get(i)+"结束时间","");
				}

			}
		}
	}
	
	/**自动设置列宽
	 * @param sheet
	 * @param columnNum
	 */
	public void setAutoWidth(XSSFSheet sheet,int columnNum) {
		
		//让列宽随着导出的列长自动适应
        for (int colNum = 0; colNum < columnNum; colNum++) {
            int columnWidth = sheet.getColumnWidth(colNum) / 256;
            for (int rowNum = 0; rowNum < sheet.getLastRowNum(); rowNum++) {
                XSSFRow currentRow;
                //当前行未被使用过
                if (sheet.getRow(rowNum) == null) {
                    currentRow = sheet.createRow(rowNum);
                } else {
                    currentRow = sheet.getRow(rowNum);
                }
                if (currentRow.getCell(colNum) != null) {
                    XSSFCell currentCell = currentRow.getCell(colNum);
                    if (currentCell.getCellType() == XSSFCell.CELL_TYPE_STRING) {
                        int length = currentCell.getStringCellValue().getBytes().length;
                        if (columnWidth < length) {
                            columnWidth = length;
                        }
                    }
                }
            }
            sheet.setColumnWidth(colNum, (columnWidth+4) * 256);         
        }
	}

}
