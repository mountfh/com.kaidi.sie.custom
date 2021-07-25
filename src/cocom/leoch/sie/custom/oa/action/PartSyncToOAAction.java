package cocom.leoch.sie.custom.oa.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.leoch.sie.custom.sap.models.PartModel;
import com.leoch.sie.custom.utils.MyPerference;
import com.teamcenter.rac.kernel.TCException;

public class PartSyncToOAAction {
	
//	private static String  url_address = "http://192.168.1.145:88/services/CreateWorkflowService";  
	private static String  url_address = null;
	private String processNum = ""; 
	@SuppressWarnings("unused")
	private HttpURLConnection getHTTPConnection() throws IOException {
		URL url = new URL(url_address);  
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
        connection.setRequestMethod("POST");  
        connection.setRequestProperty("content-type", "text/xml;charset=utf-8");  
        connection.setDoInput(true);  
        connection.setDoOutput(true); 
		return connection;
		
	}
	
	
	public  String sent(List<PartModel> models) throws IOException, TCException {
		url_address = MyPerference.getOAAddress();
		url_address = url_address+"/services/CreateWorkflowService";
		String msg = "";
		if (models == null || models.size() == 0) {
			return msg += "没有需要发送到OA的物料。";
		}
		String json = "";
		for (int i = 0; i < models.size(); i++) {
			PartModel model = models.get(i);
			Map<String,Object> value = model.getModel();
		    json += getJSON(value);	
		    if( i+1  < models.size()) {
				json += ",";
			}
		}
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
        }else {
        	msg += "物料发送OA失败（没有获取到OA的网络连接）.";
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
	   try {
		    String isSuccess = str.substring(str.indexOf("{\"isSuccess\": \"")+15, str.indexOf("\",\"message\":"));
			if(isSuccess != null && isSuccess.equals("S")) {
				processNum = str.substring(str.indexOf("\"requestid\": \"")+14, str.indexOf("\"}</ns1:out>"));
				return null;
			}else {
				return str.substring(str.indexOf("\",\"message\": \"")+14, str.indexOf("\"}</ns1:out>"));
			}
	   }catch (Exception e) {
		   return "物料发送OA失败（OA的返回结果不对）.";
	   }
	}
	

	public String getJSON(Map<String,Object> value){  
		 String info = "{"
				  +"\"MARC-WERKS\": \""+value.get(PartModel.WERKS)+"\"," 	          //<!--工厂 必填--> <!--PLM接口文档不是必填项-->
				  +"\"MARA-MTART\": \""+value.get(PartModel.MTART)+"\"," 	          //<!--物料类型 必填-->
				  +"\"MARA-MATNR\": \""+value.get(PartModel.MATNR)+"\"," 	          //<!--SAP物料编码 -->
				  +"\"MAKT-MAKTX\": \""+value.get(PartModel.MAKTX)+"\"," 	          //<!--物料描述 必填-->
				  +"\"MARA_ZEINR\": \""+value.get(PartModel.ZEINR)+"\"," 	          //<!--图号-->
				  +"\"MARA-MEINS\": \""+value.get(PartModel.MEINS)+"\"," 	          //<!--基本计量单位 必填-->
				  +"\"MARA-MATKL\": \""+value.get(PartModel.MATKL)+"\"," 	          //<!--物料组 必填-->
				  +"\"MARA-BISMT\": \""+value.get(PartModel.BISMT)+"\"," 	          //<!--旧物料号-->
				  +"\"MARA-GROES\": \""+value.get(PartModel.GROES)+"\"," 	          //<!--大小量纲-->
				  +"\"MARA-BRGEW\": \""+value.get(PartModel.BRGEW)+"\"," 	          //<!--毛重-->
				  +"\"MARA-NTGEW\": \""+value.get(PartModel.NTGEW)+"\"," 	          //<!--净重-->
				  +"\"MARA-GEWEI\": \""+value.get(PartModel.GEWEI)+"\"," 	          //<!--重量单位-->
				  +"\"MARA_NORMT\": \""+value.get(PartModel.NORMT)+"\"," 	          //<!--内部订单-->
				  +"\"MARA_FERTH\": \""+value.get(PartModel.FERTH)+"\"," 	          //<!--模穴-->
				  +"\"MARA-MSBOOKPARTNO\": \""+value.get(PartModel.MSBOOKPARTNO)+"\"," 	 //<!--产品系列--> <!--PLM接口文档未提供-->
				  +"\"GRUN\": \""+value.get(PartModel.GRUN)+"\"," 	                //<!--物料长描述-->
				  +"\"MARC-BESKZ\": \""+value.get(PartModel.BESKZ)+"\"," 	          //<!--采购类型 必填-->
				  +"\"MARC-SOBSL\": \""+value.get(PartModel.SOBSL)+"\"," 	        //<!--特殊采购类-->
				  +"\"MARC-RGEKZ\": \""+value.get(PartModel.RGEKZ)+"\""+	          //<!--反冲-->
				"}";

		 return info;	
	    }  
	 
	    public String getXML(String xml){  
	    	String soapXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"webservices.createworkflow.weaver.com.cn\">" 
	    			+"   <soapenv:Header/>" 
	    	        +"   <soapenv:Body>" 
	    			+"      <web:createWl>"  
	    	            +"    <web:in0>"  
	    	            	+" {"
	    	            		+ "\"detailtable\": ["
	    	            			+xml
	    	            		+ "]"
	    	            	+"}"
	    	            +"    </web:in0>"  
	    	        +"      </web:createWl>"  
	    	        +"   </soapenv:Body>" 
	    	        +"</soap:Envelope>";  
	        return soapXML;  
	    }  
}

