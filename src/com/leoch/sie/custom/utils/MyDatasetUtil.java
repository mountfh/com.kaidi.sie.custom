package com.leoch.sie.custom.utils;

import java.io.File;

import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCSession;

public class MyDatasetUtil {

	
	public static void replaceDatesetRelationByName(TCComponent tcc, String name, File file, String relation)
		throws Exception{
		if (relation!= null) {
			TCComponent[] coms = tcc.getRelatedComponents(relation);
			boolean flag = true;
			for (TCComponent com : coms) {
				if (com instanceof TCComponentDataset) {
					if (name.equals(com.getProperty("object_name"))) {
						flag = false;
						break;
					}
				}
			}
			if (flag) {
				TCComponentDataset dataset = createDateset(name, file, tcc.getSession());
				tcc.add(relation, dataset);
				dataset.changeOwner((TCComponentUser)tcc.getReferenceProperty("owning_user"), (TCComponentGroup)tcc.getReferenceProperty("owning_group"));
			}
		}
	}
		
	/**
	 * 通过首选项【DRAG_AND_DROP_default_dataset_type】获取文件对应的数据集类型
	 * @param file_name 文件名称/路径
	 * @param session
	 * @return 数据集类型
	 * @throws Exception
	 */
	public static String getDatasetType(String file_name, TCSession session) throws Exception {
		if (!file_name.contains(".")) {
			return "Text";
		}
		String type = null;
		String[] values = session.getPreferenceService().getStringValues("DRAG_AND_DROP_default_dataset_type");
		String suffix = file_name.substring(file_name.lastIndexOf("."), file_name.length());
		if (values != null) {
			for (String value : values) {
				String[] infos = value.split(":");
				if (infos != null && infos.length > 1) {
					if (suffix.equalsIgnoreCase("." + infos[0])) {
						type = infos[1];
						break;
					}
				}
			}
		}
		if (type == null || type.isEmpty()) {
			throw new Exception("[" + suffix + "]的文件类型未配置首选项[DRAG_AND_DROP_default_dataset_type]");
		}
		return type;
	}
	
	/**
	 * 通过数据集类型获取数据集引用类型
	 * @param file_name 文件名称/路径
	 * @param datasetType 数据集类型
	 * @param session
	 * @return 数据集类型的引用类型
	 * @throws Exception
	 */
	public static String getDatasetRefType(String file_name,String datasetType, TCSession session) throws Exception {
		if (!file_name.contains(".")) {
			return "Text";
		}
		String ref_type = "";
		TCComponentDatasetDefinitionType definitionType = (TCComponentDatasetDefinitionType) session.getTypeComponent("DatasetType");
		TCComponentDatasetDefinition def = definitionType.find(datasetType);
		NamedReferenceContext[] nameRefContexts = def.getNamedReferenceContexts();
		String suffix = file_name.substring(file_name.lastIndexOf("."), file_name.length());
		for (NamedReferenceContext nameRefContext : nameRefContexts) {
			String file_format = nameRefContext.getFileTemplate();
			if (file_format.equals("*") || file_format.equalsIgnoreCase("*" + suffix)) {
				ref_type = nameRefContext.getNamedReference();
				break;
			}
		}
		if (ref_type == null || ref_type.isEmpty()) {
			throw new Exception("[" + datasetType + "]数据集不支持此[*"+suffix+"]文件类型");
		}
		return ref_type;
	}
	
	/**
	 * 通过数据集获取数据集引用类型
	 * @param dataset 数据集
	 * @return 数据集类型的引用类型
	 * @throws Exception
	 */
	public static String getDatasetRefType(TCComponentDataset dataset) throws Exception {
		String ref_type = "";
		TCComponentDatasetDefinition def = dataset.getDatasetDefinitionComponent();
		NamedReferenceContext[] nameRefContexts = def.getNamedReferenceContexts();
		for (NamedReferenceContext nameRefContext : nameRefContexts) {
			ref_type = nameRefContext.getNamedReference();
			break;
		}
		return ref_type;
	}
	
	/**
	 * 通过数据集获取数据集文件类型
	 * @param dataset 数据集
	 * @return 数据集类型的文件类型
	 * @throws Exception
	 */
	public static String getDatasetFileType(TCComponentDataset dataset) throws Exception {
		String file_format = "";
		TCComponentDatasetDefinition def = dataset.getDatasetDefinitionComponent();
		NamedReferenceContext[] nameRefContexts = def.getNamedReferenceContexts();
		for (NamedReferenceContext nameRefContext : nameRefContexts) {
			file_format = nameRefContext.getFileTemplate();
			break;
		}
		return file_format;
	}
	
	public static TCComponentDataset createDateset(String name, File file, TCSession session)
		throws Exception{
		String file_name= file.getName();
		String datasetType = getDatasetType(file_name,  session);
		String ref = getDatasetRefType(file_name, datasetType, session);
		TCComponentDatasetType type = (TCComponentDatasetType) session.getTypeComponent("Dataset");
		TCComponentDataset dataset = type.create(name, "", datasetType);
		String[] refs = new String[] { ref };
		String[] files = new String[] { file.getAbsolutePath() };
		dataset.setFiles(files, refs);
		return dataset;
		
	}

}
