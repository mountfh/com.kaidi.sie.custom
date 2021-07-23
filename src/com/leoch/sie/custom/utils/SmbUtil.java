package com.leoch.sie.custom.utils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Date;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
 
public class SmbUtil {
	// 1. 声明属性
	private String url = "smb://userName:password@192.168.2.153/mars/";
	private SmbFile smbFile = null;
	private SmbFileOutputStream smbOut = null;
	private static SmbUtil smbUtil = null; // 共享文件协议
	
	private SmbUtil(String url) {
		this.url = url;
		this.init();
	}
	// 2. 得到SmbUtil和连接的方法
	public static synchronized SmbUtil getInstance(String url) {
		if (smbUtil == null)
			return new SmbUtil(url);
		return smbUtil;
	}
 
	
	// 3.smbFile连接
	public void init() {
		try {
			System.out.println("开始连接...url：" + this.url);
			smbFile = new SmbFile(this.url);
			smbFile.connect();
			System.out.println("连接成功...url：" + this.url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.print(e);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.print(e);
		}
	}
	
	// 4.上传文件到服务器
	public int uploadFile(File file) {
		int flag = -1;
		BufferedInputStream bf = null;
		try {
			SmbFile folder = new SmbFile(this.url);
			if(!folder.exists()){
				folder.mkdir();
			}
			this.smbOut = new SmbFileOutputStream(this.url + "/"
					+ file.getName(), false);
			bf = new BufferedInputStream(new FileInputStream(file));
			byte[] bt = new byte[8192];
			int n = bf.read(bt);
			while (n != -1) {
				this.smbOut.write(bt, 0, n);
				this.smbOut.flush();
				n = bf.read(bt);
			}
			flag = 0;
			System.out.println("文件传输结束...");
		} catch (SmbException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println(e);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.out.println("找不到主机...url：" + this.url);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(e);
		} finally {
			try {
				if (null != this.smbOut)
					this.smbOut.close();
				if (null != bf)
					bf.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
 
		return flag;
	}
	
	public static File downloadFile(String smbMachine,String localPath){
		File localfile = null;
		InputStream is = null;
		OutputStream os = null;
		try{
			//远程读取文件
			SmbFile rmiFile = new SmbFile(smbMachine);
			String filename = rmiFile.getName(); //获取文件名
			is = new BufferedInputStream(new SmbFileInputStream(rmiFile)); //对文件进行读取			
			//将远程文件写到本地
			localfile = new File(localPath + File.separator + filename);  //将远程拷贝的文件，指定到具体的本地的具体路径
			System.out.println("lcoalfile:" + localfile);
			os = new BufferedOutputStream(new FileOutputStream(localfile)); 
			int length = rmiFile.getContentLength();  //获取文件的内容大小
			System.out.println("length:" + length);
			byte[] buffer = new byte[length];
			is.read(buffer); 
			os.write(buffer);  //开始写
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				if(is!=null){
					is.close();
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return localfile;
	}
 
	// 5. 在main方法里面测试
	public static void main(String[] args) {
		System.out.println(new Date());
		// 服務器地址 格式為 smb://电脑用户名:电脑密码@电脑IP地址/IP共享的文件夹
		String remoteUrl = "smb://aaa:123456@192.168.1.145/fenfa";
		String localFile = "D:\\Share\\334.pdf"; // 本地要上传的文件
		String localPath = "D:\\Share";
//		smb://aaa:123456@192.168.1.145/2021-07-22/1.1工程更改申请流程（凯程ECR）-徐恺-2021-05-18_offline_html_83336.zip
		String smbMachine = "smb://aaa:123456@192.168.1.145/share/123R.xlsx";
		File file = new File(localFile);
		SmbUtil smb = SmbUtil.getInstance(remoteUrl);
		downloadFile(smbMachine,localPath);
//		smb.uploadFile(file);// 上传文件
//		System.out.println(new Date());
	}
}
