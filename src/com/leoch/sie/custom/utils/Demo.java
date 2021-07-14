package com.leoch.sie.custom.utils;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Color;

public class Demo extends JPanel {
	
	// 获取屏幕窗口大小
		public static final int WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
		public static final int HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
		public JFrame frame ;
	private static final long serialVersionUID = 1L;
	
	public  void start() {
		
		frame = new JFrame();
		//隐藏窗口装饰
		frame.setUndecorated(true);
		//添加到布局
		frame.getContentPane().add(new Demo(), BorderLayout.CENTER);
		// 设置窗口初始位置
		frame.setLocation((WIDTH -300) / 2, (HEIGHT - 240) / 2);
		// 设置窗口大小
		frame.setSize(300, 240);
		//显示窗口
		frame.setVisible(true);
	}

	public Demo() {
		setBackground(new Color(254, 254, 254));
		setLayout(new FlowLayout(FlowLayout.CENTER));
		System.out.println();
		JLabel label = new JLabel();
//		label.setAlignmentX(Component.CENTER_ALIGNMENT);
		label.setIcon(new ImageIcon(Demo.class.getResource("/resources/loading2.gif")));
		this.add(label);
	}
	
	public void close() {
		frame.dispose();
	}

}