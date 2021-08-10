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
    private static String PATH = null;
    public static final String DLL_FULL_NAME = "sapjco3.dll";
	private static File cfg;
	private static Properties connectProperties;
	private static JCoDestination destination = null;
	private static boolean isTest; // ��ֵΪ����ϵͳ���Ӳ���,��֮Ϊ��ʽϵͳ���Ӳ���
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
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.100.13");// ����Ӧ�÷�����
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // ϵͳ���(ʵ�����)
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "500"); // SAP����
			
//			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.100.12");// ����Ӧ�÷�����
//			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // ϵͳ���(ʵ�����)
//			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "120"); // SAP����
			
			connectProperties.setProperty(DestinationDataProvider.JCO_USER, "JT-OA"); // SAP�û���
			connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Aa123456"); // ����
			connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "zh"); // ��¼����
			connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3"); // ���������
			connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10"); // ��������߳�
		} else {
			connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, "192.168.100.14");// ��ʽӦ�÷�����
			connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR, "00"); // ϵͳ���(ʵ�����)
			connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, "800"); // SAP���š�
			
			connectProperties.setProperty(DestinationDataProvider.JCO_USER, "itport"); // SAP�û���
			connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, "Kd02779!"); // ����
			connectProperties.setProperty(DestinationDataProvider.JCO_LANG, "zh"); // ��¼����
			connectProperties.setProperty(DestinationDataProvider.JCO_POOL_CAPACITY, "3"); // ���������
			connectProperties.setProperty(DestinationDataProvider.JCO_PEAK_LIMIT, "10"); // ��������߳�
		}
		
	}
	
	/**
	 * @Title: createDataFile
	 * @Description: ��������SAP�������ļ�
	 * @param @param name
	 * @param @param suffix
	 * @param @param properties
	 * @param @throws IOException    ����
	 * @return void    ��������
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
	 * @Description: ����JCO�����ļ�
	 * @param     ����
	 * @return void    ��������
	 * @throws
	 */
        
    public static void createDll2System32() throws IOException{
    	File file = new File("C:\\Siemens\\Teamcenter12\\tccs\\lib\\");
    	try {
    		File sapjco3 = new File(DLL_SYSTEM_PATH + DLL_FULL_NAME);
    		if (!sapjco3.exists()) {
    			InputStream is = SAPConn.class.getResourceAsStream(DLL_FULL_NAME);
				FileOutputStream fos = new FileOutputStream(DLL_SYSTEM_PATH + DLL_FULL_NAME);
    			copyFile(is, fos);
			}
    		if (isTest) {
    			
//    			PATH = MyPerference.getPATHAddress();
    			if (file.exists()) {
    				PATH = "C:\\Siemens\\Teamcenter12\\tccs\\lib";
				}else {
					PATH = "D:\\Siemens\\Teamcenter12\\tccs\\lib";
				}
				
				
				System.out.println(PATH);

			} else {
//				PATH = MyPerference.getPATHAddress();
				if (file.exists()) {
    				PATH = "C:\\Siemens\\Teamcenter12\\tccs\\lib\\";
				}else {
					PATH = "D:\\Siemens\\Teamcenter12\\tccs\\lib";
				}

			}
    		sapjco3 = new File(PATH + DLL_FULL_NAME);
    		System.out.println(sapjco3);
    		if (!sapjco3.exists()) {
    			InputStream is = SAPConn.class.getResourceAsStream(DLL_FULL_NAME);
    			FileOutputStream fos = new FileOutputStream(PATH + DLL_FULL_NAME);
    			copyFile(is, fos);
			}
    	} catch (Exception e) {
    		throw new IOException("���� " + DLL_FULL_NAME + " �����ļ�����,���Թ���Ա������пͻ��ˣ�");
    	}
    }
       
    /**
     * @Title: copyFile
     * @Description: �����ļ�
     * @param @param is ������
     * @param @param fos �����
     * @param @throws IOException    ����
     * @return void    ��������
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
	 * ��ȡSAP����
	 * 
	 * @return SAP���Ӷ���
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
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		
	}

}
