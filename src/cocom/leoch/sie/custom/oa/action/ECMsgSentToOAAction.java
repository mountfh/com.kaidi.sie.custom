package cocom.leoch.sie.custom.oa.action;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.custom.bean.ECPartBean;
import com.leoch.sie.custom.utils.MyPerference;
import com.leoch.sie.custom.utils.RacDatasetUtil;
import com.leoch.sie.custom.utils.SmbUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ECMsgSentToOAAction {

	private List<TCComponentItemRevision> problemlist;
	private List<TCComponentItemRevision> solulist;
	private String url = "";	
	private TCComponentItemRevision rev;
	private ArrayList<ECPartBean> beanList;
	private HashMap<String, ArrayList<ECPartBean>> partMap;
	private String localPath = "C:\\Temp";
//	private static String  url_address = "http://192.168.1.145:88/services/PlmUploadService";  
	private static String  url_address = null;
	private TCComponentItem ecn = null;
	private String sendSmd = null;
	
	private HttpURLConnection getHTTPConnection() throws IOException {
		
		URL url = new URL(url_address);  
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");  
        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");  
        connection.setDoInput(true);  
        connection.setDoOutput(true); 
		return connection;		
	}
	
	public ECMsgSentToOAAction( TCComponentItem ecn,List<TCComponentItemRevision> problemlist,List<TCComponentItemRevision> solulist) {		
		this.ecn = ecn;
		this.problemlist = problemlist;
		this.solulist = solulist;	
		partMap = new HashMap<String, ArrayList<ECPartBean>>();
	}
	
	public String excute() throws Exception {
		url_address = MyPerference.getOAAddress();
		url_address = url_address+"/services/PlmUploadService";
		String[] SmbAddress = MyPerference.getSmbAddress();
		if(SmbAddress.length<1){
			throw new Exception("OA地址的设置有误！请配置检查首选项-K8_Smb_Address"); 
		}
		sendSmd = SmbAddress[0];
		sendSmd = sendSmd.substring(5);
		if(sendSmd.equals("")){
			throw new Exception("OA地址的设置有误！请配置检查首选项-K8_Smb_Address"); 
		}
		String msg = "";
		String json = "";
		String oaid = ecn.getProperty("object_desc");
		json += getJSON(oaid);
	   //组织SOAP数据，发送请求  
	   String soapXML = getXML(json);
	   System.out.println(soapXML);
	   HttpURLConnection connection = getHTTPConnection();
       OutputStream os = connection.getOutputStream();  
       os.write(soapXML.getBytes("UTF-8"));
        
        //接收服务端响应
        int responseCode = connection.getResponseCode();  
        if(200 == responseCode){
            InputStream is = connection.getInputStream();  
            InputStreamReader isr = new InputStreamReader(is,"UTF-8");  
            BufferedReader br = new BufferedReader(isr);               
            StringBuilder sb = new StringBuilder();  
            String temp = null;  
            while(null != (temp = br.readLine())){  
                sb.append(temp);  
            }             
            //输出返回值
            String returnMSG = getReturn(sb.toString());
            if(returnMSG != null ){
            	msg += returnMSG;
            }
            is.close();  
            isr.close();  
            br.close();
//            ecn.setProperty("k8_is_sendOA", "true");
//            ecn.setLogicalProperty("k8_is_sendOA", true);
        }else {
        	msg += "物料发送OA失败（没有获取到OA的网络连接）.";
        }  
        os.close();  
        if(msg.equals("")) {
        	try {
                ecn.setLogicalProperty("k8_is_sendOA", true);
			} catch (Exception e) {
				System.out.println(e.toString());
				msg +=e.toString();
			}

        }
		return msg;
	}
	
	public String downDatasets(TCComponent comp) throws Exception {
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		String dateString = formatter.format(date);	
//		SmbUtil smb = SmbUtil.getInstance("smb://aaa:123456@192.168.1.145/fenfa"+"/"+dateString);
		SmbUtil smb = SmbUtil.getInstance(sendSmd+"/"+dateString);
//		""\\fenfa\\2021-07-21\\机壳组件.pdf"
		String temp = localPath+"\\"+dateString;
		File folderFile = new File(temp); 
		if(!folderFile.exists()) {
			folderFile.mkdirs();
		}
		String urltemp = "";
		String path = "";
		List<TCComponentDataset> datasetList = RacDatasetUtil.getDatasets(comp);
		for (int i = 0; i < datasetList.size(); i++) {
			HashMap<String, File> fileMap = RacDatasetUtil.getTCFile(datasetList.get(i), temp);
			 if(fileMap.size()>0) {
			 for(String key:fileMap.keySet()) {
				 File value = fileMap.get(key);
				 if(value!=null) {
					 smb.uploadFile(value);
					  path = "\\fenfa\\"+value.getPath().substring(8);
					  System.out.println(path);
					  urltemp += path+"##";
				 }
			 };
		 }
		}
		if(urltemp.endsWith("##")) {
			urltemp = urltemp.substring(0,urltemp.length()-2);
		}		
		return urltemp;		
	}	
	
	public void getProblemBean() throws Exception {
		
		String type = null;
		beanList = new ArrayList<ECPartBean>();
		for (int i = 0; i < problemlist.size(); i++) {
			ECPartBean bean = new ECPartBean();
			rev = problemlist.get(i);
			type =  rev.getType();
			bean.setRevid(rev.getProperty("item_revision_id"));
			bean.setId(rev.getProperty("item_id"));
			bean.setName(rev.getProperty("object_name"));
			if(type.contains("PartRevision")) {
				bean.setType("物料");
				bean.setDesc(rev.getProperty("k8_description2"));
			}else if(type.contains("Assembly")){
				bean.setType("图纸");
				bean.setDesc(rev.getProperty("object_desc"));
			}else if(type.contains("GYRevision")){
				bean.setType("工艺");
				bean.setDesc(rev.getProperty("object_desc"));
			}else if(type.contains("Document")){
				bean.setType("文档");
				bean.setDesc(rev.getProperty("object_desc"));
			}
			url = downDatasets(rev);
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
			type =  rev.getType();
			if(type.contains("PartRevision")) {
				bean.setType("物料");
				bean.setDesc(rev.getProperty("k8_description2"));
			}else if(type.contains("Assembly")){
				bean.setType("图纸");
				bean.setDesc(rev.getProperty("object_desc"));
			}else if(type.contains("GYRevision")){
				bean.setType("工艺");
				bean.setDesc(rev.getProperty("object_desc"));
			}else if(type.contains("Document")){
				bean.setType("文档");
				bean.setDesc(rev.getProperty("object_desc"));
			}
			url = downDatasets(rev);
			bean.setUrl(url);
			beanList.add(bean);
		}
		partMap.put("S", beanList);
	}
	
//	public HashMap<String, ArrayList<ECPartBean>> getMap() throws Exception {
//		
//		getProblemBean();
//		getSoluBean();
//		return partMap;
//		
//	}
	
	private String getReturn(String str) {
		
		String msg = null;
		String responseMsg = null;
	   try {
		   responseMsg = str.substring(str.indexOf("<ns1:out>")+9, str.indexOf("</ns1:out>"));
           JSONObject demoJson =JSONObject.fromObject(responseMsg);
           String isSuccess = (String) demoJson.get("issuccess");
           msg = (String) demoJson.get("message");
		   if(isSuccess != null && isSuccess.equals("S")) {
					return "变更信息传OA成功！请在OA系统中查看EC变更表单内容!";
			}else{
					return "变更信息传OA失败！"+msg;
			}						
	   }catch (Exception e) {
		   System.out.println(e.toString());
		   return "变更信息传OA失败！"+msg;
	   }
	}
	
	public String getJSON(String oaid) throws Exception{  
		getProblemBean();
		getSoluBean();
		JSONObject JSRoot = new JSONObject();
		if(partMap.size()>0) {
	        JSONObject root = new JSONObject();
			JSONArray dataArr = new JSONArray();
			ArrayList<ECPartBean> proList = partMap.get("P");
			ArrayList<ECPartBean> solList = partMap.get("S");
			for (int i = 0; i < proList.size(); i++) {
				 ECPartBean bean = proList.get(i);
				 JSONObject jsObject = JSONObject.fromObject(bean);
				 dataArr.add(jsObject);
				 root.put("problemmsg", dataArr);
			}
			JSONArray dataArr2 = new JSONArray();
			for (int i = 0; i < solList.size(); i++) {
				 ECPartBean bean = solList.get(i);
				 JSONObject jsObject = JSONObject.fromObject(bean);
				 dataArr2.add(jsObject);
				 root.put("solutionmsg", dataArr2);
			}
			 String oaicd = ecn.getProperty("k8_OA");
			 JSRoot.put("ucode", oaicd);
			 JSRoot.put("data", root);		 
			 System.out.println(JSRoot.toString());
		}
		return JSRoot.toString();	
	}
	
    public String getXML(String xml){  
    	String soapXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"webservices.createworkflow.weaver.com.cn\">"  
    			+"   <soapenv:Header/>" 
    	        +"   <soapenv:Body>" 
    			+"      <web:PlmUploadFile>"  
    	            +"    <web:in0>" 
    	            +xml
    	            +"    </web:in0>"  
    	        +"      </web:PlmUploadFile>"  
    	        +"   </soapenv:Body>" 
    	        +"</soap:Envelope>";  
        return soapXML;  
    }  
	
}
