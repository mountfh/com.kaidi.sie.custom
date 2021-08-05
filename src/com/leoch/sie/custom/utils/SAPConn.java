package com.leoch.sie.custom.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;

public class SAPConn {

	private static final String ABAP_AS_POOLED = "ABAP_AS_WITH_POOL";
    public static final String DLL_SYSTEM_PATH = "C:\\Windows\\System32\\";
    private static String PATH;
    public static final String DLL_FULL_NAME = "sapjco3.dll"; 
    	
	private static File cfg;
	private static Properties connectProperties;
	
	private static JCoDestination destination = null;
	private static boolean isTest; // 真值为测试系统连接参数,反之为正式系统连接参数
	private static String p_name = "K8_SYSTEM_CONNET_FLAG";

	static {
		connectProperties = new Properties();
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		String flag = PreferenceUtils.getPreferenceValue(session, p_name);
		if (flag == null || flag.isEmpty()) {
			isTest = true;
		} else {
			isTest = false;
		}
		if (isTest) {
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.100.13");// 测试应用服务器
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // 系统编号(实例编号)
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "500"); // SAP集团
			
//			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.100.12");// 测试应用服务器
//			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // 系统编号(实例编号)
//			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "120"); // SAP集团
			
			connectProperties.setProperty(DestinationDataProvider.JCO_USER, "JT-OA"); // SAP用户名
			connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Aa123456"); // 密码
			connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "zh"); // 登录语言
			connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3"); // 最大连接数
			connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10"); // 最大连接线程
		} else {
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.100.14");// 正式应用服务器
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // 系统编号(实例编号)
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "800"); // SAP集团·
			
			connectProperties.setProperty(DestinationDataProvider.JCO_USER, "itport"); // SAP用户名
			connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Kd02779!"); // 密码
			connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "zh"); // 登录语言
			connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3"); // 最大连接数
			connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10"); // 最大连接线程
		}
		
	}
	
	/**
	 * @Title: createDataFile
	 * @Description: 创建连接SAP的属性文件
	 * @param @param name
	 * @param @param suffix
	 * @param @param properties
	 * @param @throws IOException    参数
	 * @return void    返回类型
	 * @throws
	 */
	    
	private static void createDataFile(String name, String suffix, Properties properties) throws IOException {
		cfg = new File(name + "." + suffix);
		if (cfg.exists()) {
			cfg.deleteOnExit();
		}
		FileOutputStream fos = new FileOutputStream(cfg, false);
		properties.store(fos, "for tests only !");
		fos.close();
	}
	   
	/**
	 * @throws IOException 
	 * @Title: createDll2System32
	 * @Description: 创建JCO驱动文件
	 * @param     参数
	 * @return void    返回类型
	 * @throws
	 */
        
    public static void createDll2System32() throws IOException{
    	try {
    		File sapjco3 = new File(DLL_SYSTEM_PATH + DLL_FULL_NAME);
    		if (!sapjco3.exists()) {
    			InputStream is = SAPConn.class.getResourceAsStream(DLL_FULL_NAME);
				FileOutputStream fos = new FileOutputStream(DLL_SYSTEM_PATH + DLL_FULL_NAME);
    			copyFile(is, fos);
			}
    		if (isTest) {
  			
//				PATH = "C:\\Siemens\\Teamcenter12\\tccs\\lib\\";

				PATH = "D:\\Siemens\\RAC4TF\\tccs\\lib\\";
				System.out.println(PATH);

			} else {

//				PATH = "C:\\Siemens\\Teamcenter12RAC4\\tccs\\lib\\";
				PATH = "D:\\Siemens\\RAC4TF\\tccs\\lib\\";

			}
    		sapjco3 = new File(PATH + DLL_FULL_NAME);
    		System.out.println(sapjco3);
    		if (!sapjco3.exists()) {
    			InputStream is = SAPConn.class.getResourceAsStream(DLL_FULL_NAME);
    			FileOutputStream fos = new FileOutputStream(PATH + DLL_FULL_NAME);
    			copyFile(is, fos);
			}
    	} catch (Exception e) {
    		throw new IOException("复制 " + DLL_FULL_NAME + " 驱动文件出错,请以管理员身份运行客户端！");
    	}
    }
       
    /**
     * @Title: copyFile
     * @Description: 复制文件
     * @param @param is 输入流
     * @param @param fos 输出流
     * @param @throws IOException    参数
     * @return void    返回类型
     * @throws
     */
        
    public static void copyFile(InputStream is, FileOutputStream fos) throws IOException{
    	int byteread = 0; 
    	byte[] buffer = new byte[1444];  
    	while ((byteread = is.read(buffer)) != -1) {
    		fos.write(buffer, 0, byteread); 
    	} 
    	is.close();
    	fos.close();
    }

	/**
	 * 获取SAP连接
	 * 
	 * @return SAP连接对象
	 * @throws IOException 
	 * @throws JCoException 
	 */
    
	public static JCoDestination connect() throws IOException, JCoException {
		if (destination == null) {
			createDll2System32();
			createDataFile(ABAP_AS_POOLED, "jcoDestination", connectProperties);
			destination = JCoDestinationManager.getDestination(ABAP_AS_POOLED);
			if (cfg != null && cfg.exists()) {
				cfg.delete();
			}
		}
		return destination;
	}
	
	public static void main(String[] args) {
		
		try {
//			JCoDestination destination = SAPConn.connect();
//			JCoRepository repository = destination.getRepository();
			String rgekz = "22:333";
			rgekz = rgekz.substring(rgekz.indexOf(":"));
			System.out.println(rgekz);
			rgekz = "22333";
			rgekz = rgekz.substring(rgekz.indexOf(":"));
			System.out.println(rgekz);
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}

}
