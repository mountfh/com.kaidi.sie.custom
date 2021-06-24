package com.leoch.sie.custom.sap.logs;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/* Log4j配置
	a.首先读取程序运行路径下的configPath下的配置。如果找不到到配置文件，则读取此类路径下的配置文件
*/
public class NewPartLog {
	//默认全局LOG对象
	public static Logger logger = Logger.getLogger("newPartLog");
	public static String configPath = "configuration" + File.separator + "newPartLog.properties";
	
	static {
		initLogConfig();
	}
	
	public static void initLogConfig() {
		String url = null;
		try {
			File file = new File(".");
			String fullPath = file.getCanonicalPath();
			if (!fullPath.endsWith(File.separator)) {
				fullPath += File.separator;
			}
			url = fullPath + configPath;
			file = new File(url);
			if (!file.exists()) {
				URL url4j = NewPartLog.class.getResource("newPartLog.properties");
				System.setProperty("log4j.configuration", url4j.toString());
				PropertyConfigurator.configure(url4j);
			} else {
				System.out.println("读取log4j配置文件成功,配置文件路径为: " + url);
				System.setProperty("log4j.configuration", url);
				PropertyConfigurator.configure(new URL("file:/" + url));
			}
		} catch (IOException e) {
			try {
				PropertyConfigurator.configure(new URL(url));
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}
	
}
