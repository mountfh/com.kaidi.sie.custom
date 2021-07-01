package com.leoch.sie.custom.sap.handlers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.stylesheet.PropertyDateComponent;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

public class ImportProjectReportHandlers extends AbstractHandler{

	private JFrame frame;
//	private PropertyDateButton button;
//	private PropertyDateButton button2;
	private PropertyDateComponent button;	
	private PropertyDateComponent button2;
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		frame = new JFrame("项目报表导出");
		frame.setSize(680, 100);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		JLabel lab = new JLabel("项目创建时间：");
		JLabel lab1 = new JLabel("-");
		JPanel panel = new JPanel();
		panel.setLayout(new PropertyLayout(5,5,5,5,5,5));
//		button = new PropertyDateButton();
//		button2 = new PropertyDateButton();
		button = new PropertyDateComponent();
		button2 = new PropertyDateComponent();
		final JButton bt2 = new JButton("后台导出");	
		panel.add(1 + ".1.right.center",lab);
		panel.add(1 + ".2.left.center",button);
		panel.add(1 + ".3.left.center",lab1);
		panel.add(1 + ".4.left.center",button2);		
		panel.add(1 + ".5.left.center",bt2);
		frame.add(panel);
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
						System.out.println(date1);
						System.out.println(date2);
						ProjectReportMain main;
						try {
							main = new ProjectReportMain(date1,date2);
							main.excute();
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

}