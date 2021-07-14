package com.leoch.sie.custom.sap.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.custom.bean.ECPartBean;
import com.leoch.sie.custom.utils.MyDataset;
import com.leoch.sie.custom.utils.MyRacDatasetFile;
import com.leoch.sie.custom.utils.RacDatasetUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ECMsgSentToOAAction {

	private List<TCComponentItemRevision> problemlist;
	private List<TCComponentItemRevision> solulist;
	private String url = "";	
	private TCComponentItemRevision rev;
	private ArrayList<ECPartBean> beanList;
	private HashMap<String, ArrayList<ECPartBean>> partMap;
	private String path = "C:\\Temp";
	private String remopath = "\\\\192.168.1.145\\share";
	
	public ECMsgSentToOAAction(List<TCComponentItemRevision> problemlist,List<TCComponentItemRevision> solulist) {		
		this.problemlist = problemlist;
		this.solulist = solulist;	
		partMap = new HashMap<String, ArrayList<ECPartBean>>();
	}
	
	public void excute() throws Exception {
		
		getProblemBean();
		getSoluBean();
		if(partMap.size()>0) {
	        JSONObject root = new JSONObject();
			JSONArray dataArr = new JSONArray();
			JSONArray dataArr1 = new JSONArray();
			ArrayList<ECPartBean> proList = partMap.get("P");
			ArrayList<ECPartBean> solList = partMap.get("S");
			for (int i = 0; i < proList.size(); i++) {
				 ECPartBean bean = proList.get(i);
				 JSONObject jsObject = JSONObject.fromObject(bean);
				 dataArr.add(jsObject);
				 root.put("ProblemMsg", dataArr);
			}
			JSONArray dataArr2 = new JSONArray();
			for (int i = 0; i < solList.size(); i++) {
				 ECPartBean bean = solList.get(i);
				 JSONObject jsObject = JSONObject.fromObject(bean);
				 dataArr2.add(jsObject);
				 root.put("SolutionMsg", dataArr2);
			}
			 JSONObject JSRoot = new JSONObject();
			 JSRoot.put("UCode", "00001");
			 JSRoot.put("Data", root);
			 
			System.out.println(JSRoot.toString());
		}
	}
	
	public String downDatasets(TCComponent comp) throws Exception {
		
		SimpleDateFormat formatter= new SimpleDateFormat("MMdd");
//		SimpleDateFormat formatter1= new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
//		String dateString1 = formatter1.format(date);
		String dateString = formatter.format(date);		
		String temp = remopath+"\\"+dateString;
		File folderFile = new File(temp); 
		if(!folderFile.exists()) {
			folderFile.mkdirs();
		}else {
			
		}
		System.out.println(temp);
		String urltemp = "";		
		List<TCComponentDataset> datasetList = RacDatasetUtil.getDatasets(comp);
		for (int i = 0; i < datasetList.size(); i++) {
			 HashMap<String, File> fileMap = RacDatasetUtil.getTCFile(datasetList.get(i), remopath);
			 if(fileMap.size()>0) {
				 for(String key:fileMap.keySet()) {
					 File value = fileMap.get(key);
					 if(value!=null) {
						  urltemp += value.getPath()+",";
					 }
				 };
			 }
		}
		if(urltemp.endsWith(",")) {
			urltemp = urltemp.substring(0,urltemp.length()-1);
		}
		
		System.out.println(urltemp);
		return urltemp;
		
	}
	

	public static void smbPut(String remoteUrl, File localFile) {
		InputStream in = null;
		OutputStream out = null;
		try {
//			File localFile = new File(localFilePath);
			String fileName = localFile.getName();
			SmbFile remoteFile = new SmbFile(remoteUrl + "/" + fileName);
			in = new BufferedInputStream(new FileInputStream(localFile));
			out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
			byte[] buffer = new byte[1024];
			while (in.read(buffer) != -1) {
				out.write(buffer);
				buffer = new byte[1024];
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void getProblemBean() throws TCException {
		
		String type = null;
		beanList = new ArrayList<ECPartBean>();
		for (int i = 0; i < problemlist.size(); i++) {
			ECPartBean bean = new ECPartBean();
			rev = problemlist.get(i);
			type =  rev.getType();
			System.out.println(type);
			if(type.contains("PartRevision")) {
				bean.setType("物料");
			}else if(type.contains("Assembly")){
				bean.setType("图纸");
			}
			bean.setRevid(rev.getProperty("item_revision_id"));
			bean.setId(rev.getProperty("item_id"));
			bean.setName(rev.getProperty("object_name"));
			bean.setDesc(rev.getProperty("k8_description1"));
			bean.setUrl(url);
			beanList.add(bean);
		}
		partMap.put("P", beanList);
	}
	
	public void getSoluBean() throws Exception {
		
		String type = null;
		beanList = new ArrayList<ECPartBean>();
		for (int i = 0; i < solulist.size(); i++) {
			ECPartBean bean = new ECPartBean();
			rev = solulist.get(i);
			bean.setRevid(rev.getProperty("item_revision_id"));
			bean.setId(rev.getProperty("item_id"));
			bean.setName(rev.getProperty("object_name"));
			bean.setDesc(rev.getProperty("k8_description1"));
			type =  rev.getType();
			System.out.println(type);
			if(type.contains("PartRevision")) {
				bean.setType("物料");
			}else if(type.contains("Assembly")){
				bean.setType("图纸");
			}
			url = downDatasets(rev);
			bean.setUrl(url);
			beanList.add(bean);
		}
		partMap.put("S", beanList);
	}
	
	public HashMap<String, ArrayList<ECPartBean>> getMap() throws Exception {
		
		getProblemBean();
		getSoluBean();
		return partMap;
		
	}
	
}
