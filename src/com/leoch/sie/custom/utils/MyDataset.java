package com.leoch.sie.custom.utils;

import java.io.File;
import java.io.IOException;

import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

public class MyDataset {

	/**
	 * @Title: createDateset
	 * @Description: TODO(创建数据集，并弹出是否打开数据的对话框)
	 * @param @param revision
	 * @param @param file
	 * @param @throws TCException
	 * @param @throws IOException 参数
	 * @return void 返回类型
	 * @throws
	 */

	public static TCComponentDataset createDateset(String name, File file, TCSession session)
		throws TCException, IOException {
		String fileType = getFileType(file);
		String ref = getrefType(fileType);
		TCComponentDatasetType type = (TCComponentDatasetType) session.getTypeService().getTypeComponent("Dataset");
		TCComponentDataset dataset = type.create(name, "", fileType);
		String[] refs = new String[] { ref };
		String[] files = new String[] { file.getAbsolutePath() };
		dataset.setFiles(files, refs);
		return dataset;
	}

	/**
	 * @Title: getrefType
	 * @Description: TODO(获取TC文件类型对应的关系类型)
	 * @param @param fileType
	 * @param @return
	 * @param @throws TCException 参数
	 * @return String 返回类型
	 * @throws
	 */

	public static String getrefType(String fileType) throws TCException {
		String refType = null;
		if (fileType.contains("MSExcel")) {
			refType = "excel";
		} else if (fileType.contains("MSWord")) {
			refType = "word";
		} else if (fileType.contains("MSProwerPoint")) {
			refType = "powerpoint";
		} else if (fileType.contains("Zip")) {
			refType = "ZIPFILE";
		} else if (fileType.contains("PDF")) {
			refType = "PDF_Reference";
		} else if (fileType.contains("Image")) {
			refType = "Image";
		} else if (fileType.contains("Text")) {
			refType = "Text";
		}

		if (refType == null) {
			throw new TCException("不支持的文件类型！");
		}
		return refType;
	}

	/**
	 * @Title: getFileType
	 * @Description: TODO(获取文件在TC对应的文件类型)
	 * @param @param file
	 * @param @return
	 * @param @throws TCException 参数
	 * @return String 返回类型
	 * @throws
	 */

	public static String getFileType(File file) throws TCException {
		String datesetType = null;
		if (file == null) {
			throw new TCException("不支持的文件类型！");
		}
		String fileName = file.getName();
		if (fileName.endsWith("xls")) {
			datesetType = "MSExcel";
		} else if (fileName.endsWith("xlsx")) {
			datesetType = "MSExcelX";
		} else if (fileName.endsWith("doc")) {
			datesetType = "MSWord";
		} else if (fileName.endsWith("docx")) {
			datesetType = "MSWordX";
		} else if (fileName.endsWith("ppt")) {
			datesetType = "MSProwerPoint";
		} else if (fileName.endsWith("pptx")) {
			datesetType = "MSProwerPointX";
		} else if (fileName.endsWith("zip") || fileName.endsWith("rar") || fileName.endsWith("7z")) {
			datesetType = "Zip";
		} else if (fileName.endsWith("pdf")) {
			datesetType = "PDF";
		} else if (fileName.endsWith("jpg") || fileName.endsWith("jpeg")|| fileName.endsWith("png")|| fileName.endsWith("xps")) {
			datesetType = "Image";
		} else if (fileName.endsWith("txt")) {
			datesetType = "Text";
		}

		if (datesetType == null) {
			throw new TCException("不支持的文件类型！");
		}
		return datesetType;
	}

	/**
	 * @throws TCException 
	 * @Title: getFile
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @param @param dataset
	 * @param @return    参数
	 * @return File    返回类型
	 * @throws
	 */
		    
	public static File getFile(TCComponentDataset dataset) throws TCException{
		File datasetFile = null;
		String filePath = System.getProperty("user.home");
		TCComponentDatasetDefinition df = (TCComponentDatasetDefinition) dataset.getDatasetDefinitionComponent();
		NamedReferenceContext nameRefContexts[] = df.getNamedReferenceContexts();
		if (nameRefContexts == null || nameRefContexts.length == 0){
			return datasetFile;
		}
		NamedReferenceContext nf = nameRefContexts[0];
		String namedRef = nf.getNamedReference();
		String[] fileNames = dataset.getFileNames(namedRef);
		if (fileNames == null || fileNames.length == 0){
			return datasetFile;
		}
		for (String fileName : fileNames) {
			File file = dataset.getFile(namedRef, fileName, filePath);
			if (file != null && file.exists()) {
				datasetFile = file;
				break;
			}
		}		
		return datasetFile;
	}
	
	public static File[] getFilesByName(TCSession session, String name, String refName) throws Exception{
		TCComponent[] comps = session.search("数据集...", new String[]{"所有权用户","名称"},new String[]{"infodba",name});
		if(comps == null || comps.length < 1){
			return null;
		}
		if(comps == null || comps.length < 1){
			return null;
		}
		TCComponentDataset ds = (TCComponentDataset) comps[0];
		String path = System.getProperty("user.home"); 
		if (!path.endsWith(File.separator)) {
			path += File.separator;
		}
		String scriptFolder = path + "scriptPath";
		File folder = new File(scriptFolder);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File[] files = ds.getFiles(refName, scriptFolder);
		return files;
	}
	
	public static void setFiles(TCComponentDataset ds, File file) throws TCException{
		String fileType = MyDataset.getFileType(file);
		String refType = MyDataset.getrefType(fileType);
		String[] refTypes = new String[]{ refType };
		String[] files = new String[]{ file.getAbsolutePath() };
		ds.setFiles(files, refTypes);
	}
}
