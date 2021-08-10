package com.leoch.sie.custom.sap.handlers;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.leoch.sie.custom.utils.MyCreateUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.stylesheet.PropertyDateButton;
import com.teamcenter.rac.stylesheet.PropertyDateComponent;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

import cocom.leoch.sie.custom.oa.action.BOMCompareTool;
import cocom.leoch.sie.custom.oa.action.BOMInfo;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class ImportProjectReportHandlers extends AbstractHandler{
	
	private TCSession session = (TCSession) AIFUtility.getDefaultSession();	
	private ArrayList<BOMInfo> BOMInfoList = new ArrayList<>();
	private String parentID = null;

	private JFrame frame = null;
	private JTextField exportPath;
//	private PropertyDateComponent button;	
//	private PropertyDateComponent button2;
	private PropertyDateButton button;
	private PropertyDateButton button2;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
//		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//注意月份是MM
//        try {
//			Date date1 = simpleDateFormat.parse("2021-07-30");
//			InterfaceAIFComponent com = AIFUtility.getCurrentApplication().getTargetComponent();
//			if(com instanceof TCComponentItemRevision){
//				Date date2 = ((TCComponentItemRevision) com).getDateProperty("last_mod_date");
//				if(date2.before(date1)){
//					System.out.println("date1>date2");
//				}else{
//					System.out.println("date1<date2");
//				}
//			}
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		String smbMachine = "smb://aaa:123456@192.168.1.145/share/123R.xlsx";
//		String localPath = "D:\\Share";
//		String remoteUrl = "smb://aaa:123456@192.168.1.145/fenfa";
//		File file = readFromSmb(smbMachine,localPath);
//		smbPut(remoteUrl,"D:\\Share\\123R.xlsx");
//		if(file.exists()){
//			System.out.println("okokok");
//		}

		InterfaceAIFComponent[] aifComponent = AIFUtility.getCurrentApplication().getTargetComponents();
		
		test(aifComponent);
		
//		String smbMachine = "smb://aaa:123456@192.168.1.145/share/123R.xlsx";
//		String localPath = "D:\\Share";
//		String remoteUrl = "smb://aaa:123456@192.168.1.145/fenfa";
//		File file = readFromSmb(smbMachine,localPath);
////		smbPut(remoteUrl,"D:\\Share\\123R.xlsx");
//		if(file.exists()){
//			System.out.println("okokok");
//		}
        return null;
        }
	
	public void test(InterfaceAIFComponent[] targets) {
		System.out.println("测试");
		BOMCompareTool bomct = new BOMCompareTool(targets);
		TCComponentBOMWindowType bomWindowType;
		TCComponentBOMWindow window = null;
		
		try {
			bomWindowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
			window = bomWindowType.create(null);
			for (InterfaceAIFComponent ac : targets) {
				List<TCComponentBOMLine> NewBOMLine = new ArrayList<>();
				List<TCComponentBOMLine> OldBOMLine = new ArrayList<>();
				if(ac instanceof TCComponentItem){
					TCComponentItem item = (TCComponentItem) ac;
					parentID = item.getProperty("item_id");
					TCComponentBOMLine bomLine = window.setWindowTopLine(item, item.getLatestItemRevision(), null, null);	
				    
				}else if(ac instanceof TCComponentItemRevision){
					TCComponentItemRevision itemRev = (TCComponentItemRevision) ac;
					parentID = itemRev.getProperty("item_id");
					TCComponentBOMLine bomLine = window.setWindowTopLine(itemRev.getItem(), itemRev, null, null);
					NewBOMLine =bomct.getNewBOMLine(bomLine, NewBOMLine);
					OldBOMLine = bomct.getOldBOMLine(itemRev,OldBOMLine);	
					if(NewBOMLine.size()>0&&OldBOMLine.size()>0) {
						bomct.CompareBom(NewBOMLine, OldBOMLine, bomLine, itemRev);
					}else if(NewBOMLine.size()==0&&OldBOMLine.size()>0) {
						for (int i = 0; i <OldBOMLine.size(); i++) {
							BOMInfo info = new BOMInfo(bomLine,OldBOMLine.get(i),"0","3");
							BOMInfoList.add(info);
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}		
	}
	
	
		
//		Map<String, String> propertyMap = new HashMap<String, String>();
//		InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
//		TCComponentItemRevision rev = (TCComponentItemRevision) comp;
//
//		propertyMap.put("k8_part", "1103");
//		propertyMap.put("k8_group", "G1102");
//		propertyMap.put("k8_groupcount", "G1102999");
//		propertyMap.put("k8_status", "add");
//		String temp = null;
//		TCComponent row = MyCreateUtil.createWorkspaceObject("K8_ProcessRow", propertyMap);
//		 try {
//			TCComponent[] comps = rev.getRelatedComponents("k8_row");
//			if(comps.length>0){
//				for (int i = 0; i < comps.length; i++) {
//					temp = comps[i].getProperty("k8_part");
//					if(temp.equals("1102")){
//						rev.remove("k8_row", comps[i]);
//						rev.add("k8_row", row);
//					}
//				}
//			}
//			rev.add("k8_row", row);
//		} catch (TCException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
		
//		frame = new JFrame("项目报表导出");
//		frame.setSize(680, 200);
//		frame.setLocationRelativeTo(null);	
//		frame.setAlwaysOnTop(true);
//		JLabel lab = new JLabel("项目创建时间：");
//		JLabel lab1 = new JLabel("-");
//		JPanel panel = new JPanel();
//		JPanel panel2 = new JPanel();
//		panel.setPreferredSize(new Dimension(670, 200));
//		panel.setLayout(new PropertyLayout(5,5,5,5,5,5));
////		panel2.setLayout(new PropertyLayout(5,5,5,5,5,5));
//		button = new PropertyDateButton();
//		button2 = new PropertyDateButton();
////		button = new PropertyDateComponent();
////		button2 = new PropertyDateComponent();
//		final JButton bt2 = new JButton("后台导出");	
//		JButton searchBtn = new JButton("浏览...");
//		exportPath = new JTextField(15);
//		exportPath.setEditable(false);
//		JLabel addressLab = new JLabel("所选路径：");
//		int num = 1;
//		panel.add(num + ".1.center.center",lab);
//		panel.add(num + ".2.center.center",button);
//		panel.add(num + ".3.center.center",lab1);
//		panel.add(num++ + ".4.center.center",button2);
//		panel.add(num + ".1.center.center",addressLab);
//		panel.add(num + ".2.center.center",exportPath);
//		panel.add(num + ".3.center.center",searchBtn);
//		panel.add(num + ".4.center.center",bt2);
//		bt2.setEnabled(false);
//		frame.add(panel);
////		frame.add(panel2);
//		frame.setVisible(true);
//		searchBtn.addActionListener(new ActionListener() {
//			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				String path = showSelectFileDialog();
//				exportPath.setText(path);
//				bt2.setEnabled(true);
//			}
//		});
//		// TODO Auto-generated method stub
//		bt2.addActionListener(new ActionListener() {			
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				bt2.setEnabled(false);
//				new Thread(new Runnable() {			
//					@Override
//					public void run() {	
//						
//						Date date1 = button.getDate();
//						Date date2 = button2.getDate();
//						ProjectReportMain main;
//						try {
//							String path= exportPath.getText();
//							if(path!=null){
//								main = new ProjectReportMain(date1,date2,path);
//								main.excute();
//							}
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							MessageBox.post(e.toString(), "错误", MessageBox.ERROR);
//							e.printStackTrace();
//						}
//					}
//				}).start();		
//				frame.setVisible(false);
//			}
//		});
//		return null;
//	}
	
	public void smbPut(String remoteUrl,String localFilePath) {
		InputStream in = null;

		OutputStream out = null;

		try {
			File localFile = new File(localFilePath);

			String fileName = localFile.getName();

			SmbFile remoteFile = new SmbFile(remoteUrl+"/"+fileName);

			in = new BufferedInputStream(new FileInputStream(localFile));

			out = new BufferedOutputStream(new SmbFileOutputStream(remoteFile));
			byte[] buffer = new byte[1024];
			while(in.read(buffer)!=-1){
				out.write(buffer);
				buffer = new byte[1024];
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public static File readFromSmb(String smbMachine,String localPath){
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
				is.close();
				os.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return localfile;
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