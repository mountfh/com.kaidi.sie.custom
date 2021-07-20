package com.leoch.sie.custom.sap.dialog;

import java.awt.Frame;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

public class SelectFileDialog {

	Frame frame = null;
	String path = null;
	File file = null;
	
	public SelectFileDialog(Frame frame){
	
		this.frame = frame;
	}
	
	public void showSelectFileDialog(){
		int result = 0;
		
		JFileChooser fileChooser = new JFileChooser();
		
		FileSystemView fsv = FileSystemView.getFileSystemView(); //注意了，这里重要的一句
		
		System.out.println("桌面路径"+fsv.getHomeDirectory()); //得到桌面路径
		
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		
		fileChooser.setDialogTitle("请选择下载路径...");
		fileChooser.setApproveButtonText("确定");
		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		result = fileChooser.showOpenDialog(frame);
		
		if (JFileChooser.APPROVE_OPTION == result) {
			path=fileChooser.getSelectedFile().getPath();
			//System.out.println("path: "+path);
		}
	}
	
	public String getSelectFilePath(){
		return path;
	}
}
