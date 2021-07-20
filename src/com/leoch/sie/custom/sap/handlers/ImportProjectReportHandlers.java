package com.leoch.sie.custom.sap.handlers;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileSystemView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.stylesheet.PropertyDateButton;
import com.teamcenter.rac.stylesheet.PropertyDateComponent;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

public class ImportProjectReportHandlers extends AbstractHandler{

	private JFrame frame = null;
	private JTextField exportPath;
//	private PropertyDateComponent button;	
//	private PropertyDateComponent button2;
	private PropertyDateButton button;
	private PropertyDateButton button2;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		frame = new JFrame("项目报表导出");
		frame.setSize(680, 200);
		frame.setLocationRelativeTo(null);	
		frame.setAlwaysOnTop(true);
		JLabel lab = new JLabel("项目创建时间：");
		JLabel lab1 = new JLabel("-");
		JPanel panel = new JPanel();
		JPanel panel2 = new JPanel();
		panel.setPreferredSize(new Dimension(670, 200));
		panel.setLayout(new PropertyLayout(5,5,5,5,5,5));
//		panel2.setLayout(new PropertyLayout(5,5,5,5,5,5));
		button = new PropertyDateButton();
		button2 = new PropertyDateButton();
//		button = new PropertyDateComponent();
//		button2 = new PropertyDateComponent();
		final JButton bt2 = new JButton("后台导出");	
		JButton searchBtn = new JButton("浏览...");
		exportPath = new JTextField(15);
		exportPath.setEditable(false);
		JLabel addressLab = new JLabel("所选路径：");
		int num = 1;
		panel.add(num + ".1.center.center",lab);
		panel.add(num + ".2.center.center",button);
		panel.add(num + ".3.center.center",lab1);
		panel.add(num++ + ".4.center.center",button2);
		panel.add(num + ".1.center.center",addressLab);
		panel.add(num + ".2.center.center",exportPath);
		panel.add(num + ".3.center.center",searchBtn);
		panel.add(num + ".4.center.center",bt2);
		bt2.setEnabled(false);
		frame.add(panel);
//		frame.add(panel2);
		frame.setVisible(true);
		searchBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String path = showSelectFileDialog();
				exportPath.setText(path);
				bt2.setEnabled(true);
			}
		});
		// TODO Auto-generated method stub
		bt2.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				bt2.setEnabled(false);
				new Thread(new Runnable() {			
					@Override
					public void run() {	
						
						Date date1 = button.getDate();
						Date date2 = button2.getDate();
						ProjectReportMain main;
						try {
							String path= exportPath.getText();
							if(path!=null){
								main = new ProjectReportMain(date1,date2,path);
								main.excute();
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							MessageBox.post(e.toString(), "错误", MessageBox.ERROR);
							e.printStackTrace();
						}
					}
				}).start();		
				frame.setVisible(false);
			}
		});
		return null;
	}
	
	public String showSelectFileDialog(){
		int result = 0;
		
		JFileChooser fileChooser = new JFileChooser();
		
		FileSystemView fsv = FileSystemView.getFileSystemView(); //注意了，这里重要的一句
		
		System.out.println("桌面路径"+fsv.getHomeDirectory()); //得到桌面路径
		
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		
		fileChooser.setDialogTitle("请选择下载路径...");
		fileChooser.setApproveButtonText("确定");
		
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		result = fileChooser.showOpenDialog(frame);
		String selectpath = null;
		if (JFileChooser.APPROVE_OPTION == result) {
			selectpath=fileChooser.getSelectedFile().getPath();
			//System.out.println("path: "+path);
		}
		
		return selectpath;
	}
	

}