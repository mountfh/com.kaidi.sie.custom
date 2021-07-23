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
import java.util.Date;

import com.leoch.sie.custom.utils.MyDatasetUtil;
import com.leoch.sie.custom.utils.SmbUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

import jcifs.smb.SmbFile;
import net.sf.json.JSONObject;

public class GetOADocAction {

	private static String  url_address = "http://192.168.1.145:88/services/PlmDownloadService";  
	private String processNum = ""; 
	private TCSession session = null;
	private TCComponentItem ecn = null;
	private String localPath = "D:\\Temp";
	private String dateString = null;
	
	private HttpURLConnection getHTTPConnection() throws IOException {
		URL url = new URL(url_address);  
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
        connection.setRequestMethod("POST");  
        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");  
        connection.setDoInput(true);  
        connection.setDoOutput(true); 
		return connection;		
	}
	
	public  String sent(String oaid,TCComponentItem ecn) throws IOException, TCException {
		this.ecn = ecn;
		String msg = "";
		String json = "";
		json += getJSON(oaid);
	   //组织SOAP数据，发送请求  
	   String soapXML = getXML(json);
	   System.out.println(soapXML);
	   HttpURLConnection connection = getHTTPConnection();
       OutputStream os = connection.getOutputStream();  
       os.write(soapXML.getBytes("UTF-8"));
       session = (TCSession) AIFUtility.getDefaultSession();
        
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
//            System.out.println(sb.toString());
            String returnMSG = getReturn(sb.toString());
            if(returnMSG != null ){
            	msg += returnMSG;
            }
            is.close();  
            isr.close();  
            br.close();
        }else {
        	msg += "获取OA附件失败（没有获取到OA的网络连接）.";
        }  
        os.close();  
		return msg;
	}
	 
	 public String getProcessNum() {
		return processNum;
	}


	public void setProcessNum(String processNum) {
		this.processNum = processNum;
	}


	private String getReturn(String str) {
		
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date();
		dateString = formatter.format(date);	
		String msg = null;
		String responseMsg = null;
	   try {
		   responseMsg = str.substring(str.indexOf("<ns1:out>")+9, str.indexOf("</ns1:out>"));
           JSONObject demoJson =JSONObject.fromObject(responseMsg);
           String isSuccess = (String) demoJson.get("issuccess");
           String issend = (String) demoJson.get("isend");
           String node = (String) demoJson.get("nodename");
           String oamsg = (String) demoJson.get("message");
           String formfilepath = demoJson.getString("formfilepath");
           String formname = (String) demoJson.get("formfilename");
           System.out.println(isSuccess);
		   if(isSuccess != null && isSuccess.equals("S")) {
				if(issend!=null&&issend.equals("1")){
					if(!formfilepath.equals("")&&!formname.equals("")){
//						pigeonhole(formfilepath+"\\"+formname);
						pigeonhole(formname);
					}
					return "OA变更表单附件归档成功！归档在EC的变更文件夹中!";
				}else if(issend!=null&&issend.equals("0")){
					msg = "变更流程尚未走完，无法归档！当前节点为："+node;
					return msg;
				}else{
					return "接口传送成功，但返回字段有误！";
				}			
			}else if(isSuccess!=null&&isSuccess.equals("F")){
				return oamsg;
			}else{
				return null;
			}
	   }catch (Exception e) {
		   System.out.println(e.toString());
		   return e.toString()+","+responseMsg;
	   }
	}
	
	public void  pigeonhole(String filename) throws Exception{
		String docname = "";
		TCComponentDataset dataset = null;
		File folder = new File(localPath);
		if (!folder.exists()) {
			folder.mkdir();
		}
		if (filename != null) {
//			System.out.println(filename);
			String smb = "smb://aaa:123456@192.168.1.145/share/"+dateString+"/"+filename;
			File file = SmbUtil.downloadFile(smb, localPath);
			if(file.exists()){
				docname = file.getName();
				dataset =MyDatasetUtil.createDateset(docname, file, session);
				ecn.add("K8_ECRlist", dataset);
			}else{
				throw new Exception("OA的上传文件不存在！或无法访问OA路径文件。");
			}

		}
	}	
	
	public String getJSON(String oaid){  
		 String info = "{"
				  +"\"requestid\": \""+oaid+"\""+ 	          //<!-OA流程ID-->
				"}";
		 return info;	
	    }  
	 
	    public String getXML(String xml){  
	    	String soapXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"webservices.createworkflow.weaver.com.cn\">"  
	    			+"   <soapenv:Header/>" 
	    	        +"   <soapenv:Body>" 
	    			+"      <web:PlmDownloadFile>"  
	    	            +"    <web:in0>" 
	    	            +xml
	    	            +"    </web:in0>"  
	    	        +"      </web:PlmDownloadFile>"  
	    	        +"   </soapenv:Body>" 
	    	        +"</soap:Envelope>";  
	        return soapXML;  
	    }  
	
}
