package cocom.leoch.sie.custom.oa.action;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.leoch.sie.custom.utils.NumberValidationUtils;
 
public class Test {
	
    public static void main(String[] args) throws IOException {  
    	double topQuantity = 10000;
    	String  quantity = "0.00013";
//    	Double p = Double.parseDouble(quantity);
    	Double p = Double.valueOf(quantity).doubleValue();
    	Double pp =  (p * topQuantity);
    	DecimalFormat df = new DecimalFormat("#0.0000000");
    	quantity = df.format(pp);
		boolean b = NumberValidationUtils.isQuantityNumber1(quantity);
		 p = Double.parseDouble(quantity);
    	System.out.println(p);
//    	 URL url = new URL("http://192.168.1.145:88/services/CreateWorkflowService");  
//         HttpURLConnection connection = (HttpURLConnection) url.openConnection();  
//         connection.setRequestMethod("POST");  
//         connection.setRequestProperty("content-type", "text/xml;charset=utf-8");  
//         connection.setDoInput(true);  
//         connection.setDoOutput(true);  
//   
//         //锟斤拷织SOAP锟斤拷锟捷ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷  
//         String soapXML = getXML("{    \"detailtable\": [        {            \"MARC-WERKS\": \"8000\",             \"MARA-MTART\": \"Z003\",             \"MARA-MATNR\": \"10400510\",             \"MAKT-MAKTX\": \"定位块PT007/PVC*L=22/灰色\",             \"MARA_ZEINR\": \"AQQ8.208.028\",             \"MARA-MEINS\": \"EA\",             \"MARA-MATKL\": \"104001\",             \"MARA-BISMT\": \"801600033\",             \"MARA-GROES\": \"PT007/PVC*L=22/灰色\",             \"MARA-BRGEW\": \"0.1\",             \"MARA-NTGEW\": \"0.1\",             \"MARA-GEWEI\": \"KG\",             \"MARA_NORMT\": \"CFYA2021001\",             \"MARA_FERTH\": \"1\",             \"MARA-MSBOOKPARTNO\": \"1\",             \"GRUN\": \"定位块/KDPT007PVC(灰色)/L=22\",             \"MARC-BESKZ\": \"F\",             \"MARC-SOBSL\": \"30\",             \"MARC-RGEKZ\": \"1\"        },         {            \"MARC-WERKS\": \"8010\",             \"MARA-MTART\": \"Z003\",             \"MARA-MATNR\": \"10400510\",             \"MAKT-MAKTX\": \"定位块PT007/PVC*L=22/灰色\",             \"MARA_ZEINR\": \"AQQ8.208.028\",             \"MARA-MEINS\": \"EA\",             \"MARA-MATKL\": \"104001\",             \"MARA-BISMT\": \"801600033\",             \"MARA-GROES\": \"PT007/PVC*L=22/灰色\",             \"MARA-BRGEW\": \"0.1\",             \"MARA-NTGEW\": \"0.1\",             \"MARA-GEWEI\": \"KG\",             \"MARA_NORMT\": \"CFYA2021001\",             \"MARA_FERTH\": \"1\",             \"MARA-MSBOOKPARTNO\": \"1\",             \"GRUN\": \"定位块/KDPT007PVC(灰色)/L=22\",             \"MARC-BESKZ\": \"F\",             \"MARC-SOBSL\": \"30\",             \"MARC-RGEKZ\": \"\"        }    ]}");  
//         System.out.println(soapXML);
//         OutputStream os = connection.getOutputStream();  
//         os.write(soapXML.getBytes("UTF-8"));   
//        
//        //
//        int responseCode = connection.getResponseCode();  
//        if(200 == responseCode){
//            InputStream is = connection.getInputStream();  
//            InputStreamReader isr = new InputStreamReader(is);  
//            BufferedReader br = new BufferedReader(isr);  
//              
//            StringBuilder sb = new StringBuilder();  
//            String temp = null;  
//            while(null != (temp = br.readLine())){  
//                sb.append(temp);  
//            }  
//            
//            //
//            String str = sb.toString();
//            System.out.println(str);
//            String isSuccess = str.substring(str.indexOf("{\"isSuccess\": \"")+15, str.indexOf("\",\"message\":"));
//			String message = str.substring(str.indexOf("\",\"message\": \"")+14, str.indexOf("\",\"requestid\":"));
//            is.close();  
//            isr.close();  
//            br.close();  
//        }  
//        os.close();  
    }  
 
 
    public static String getXML(String xml){  
    	String soapXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"webservices.createworkflow.weaver.com.cn\">" 
    			+"   <soapenv:Header/>" 
    	        +"   <soapenv:Body>" 
    			+"      <web:createWl>"  
    	            +"    <web:in0>"  
    	            	+xml
    	            +"    </web:in0>"  
    	        +"      </web:createWl>"  
    	        +"   </soapenv:Body>" 
    	        +"</soap:Envelope>";  
        return soapXML;  
    }  
    
    public static String getUTF8StringFromGBKString(String gbkStr) {
        try {
            return new String(getUTF8BytesFromGBKString(gbkStr), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new InternalError();
        }
    }

    public static byte[] getUTF8BytesFromGBKString(String gbkStr) {
        int n = gbkStr.length();
        byte[] utfBytes = new byte[3 * n];
        int k = 0;
        for (int i = 0; i < n; i++) {
            int m = gbkStr.charAt(i);
            if (m < 128 && m >= 0) {
                utfBytes[k++] = (byte) m;
                continue;
            }
            utfBytes[k++] = (byte) (0xe0 | (m >> 12));
            utfBytes[k++] = (byte) (0x80 | ((m >> 6) & 0x3f));
            utfBytes[k++] = (byte) (0x80 | (m & 0x3f));
        }
        if (k < utfBytes.length) {
            byte[] tmp = new byte[k];
            System.arraycopy(utfBytes, 0, tmp, 0, k);
            return tmp;
        }
        return utfBytes;
    }
}