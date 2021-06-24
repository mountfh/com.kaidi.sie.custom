package com.leoch.sie.custom.sap.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.stylesheet.PropertyNameLabel;
import com.teamcenter.rac.stylesheet.PropertyTextField;
import com.teamcenter.rac.util.PropertyLayout;

public class ViewPartStockDialog extends AbstractAIFDialog{
	    
	private static final long serialVersionUID = 2421230560256304581L;
		
	/**
	 * 查看库存界面
	 *
	 * @param part 选中的物料
	 * @param values SAP库存信息
	 * @throws Exception
	 */
	    
	public ViewPartStockDialog(TCComponentItemRevision part,String[][] values) throws Exception {
		super(AIFUtility.getActiveDesktop());
		setTitle("查看库存和采购价格");		
		JPanel panel = new JPanel();
		setLayout(new BorderLayout());
		getContentPane().add(panel,BorderLayout.CENTER);
		panel.setLayout(new BorderLayout());
		JPanel partInfo = new JPanel();
		partInfo.setBackground(Color.white);
		panel.add(partInfo,BorderLayout.NORTH);
		partInfo.setBorder(BorderFactory.createTitledBorder("零件信息"));
		partInfo.setLayout(new PropertyLayout());
		int row = 1;
		TCProperty tcp = part.getTCProperty("item_id");
		PropertyNameLabel label = new PropertyNameLabel();
		label.load(tcp);
		PropertyTextField textField = new PropertyTextField();
		textField.load(tcp);
		textField.setColumns(15);
		textField.setEditable(false);
		partInfo.add(row + ".1.right.center", label);
		partInfo.add(row++ + ".2.right.center.resizable", textField);
		
		tcp = part.getTCProperty("object_name");
		label = new PropertyNameLabel();
		label.load(tcp);
		textField = new PropertyTextField();
		textField.load(tcp);
		textField.setColumns(15);
		textField.setEditable(false);
		partInfo.add(row + ".1.right.center", label);
		partInfo.add(row++ + ".2.right.center.resizable", textField);
		
		tcp = part.getTCProperty("item_revision_id");
		label = new PropertyNameLabel();
		label.load(tcp);
		textField = new PropertyTextField();
		textField.load(tcp);
		textField.setColumns(15);
		textField.setEditable(false);
		partInfo.add(row + ".1.right.center", label);
		partInfo.add(row++ + ".2.right.center.resizable", textField);
		
		tcp = part.getTCProperty("object_desc");
		label = new PropertyNameLabel();
		label.load(tcp);
		textField = new PropertyTextField();
		textField.load(tcp);
		textField.setColumns(15);
		textField.setEditable(false);
		partInfo.add(row + ".1.right.center", label);
		partInfo.add(row++ + ".2.right.center.resizable", textField);
		
		tcp = part.getTCProperty("release_status_list");
		label = new PropertyNameLabel();
		label.load(tcp);
		textField = new PropertyTextField();
		textField.load(tcp);
		textField.setColumns(15);
		textField.setEditable(false);
		partInfo.add(row + ".1.right.center", label);
		partInfo.add(row++ + ".2.right.center.resizable", textField);
				
		JPanel sapInfo = new JPanel();
		sapInfo.setBackground(Color.white);
		sapInfo.setBorder(BorderFactory.createTitledBorder("SAP库存和价格信息"));
		panel.add(sapInfo,BorderLayout.CENTER);
		sapInfo.setLayout(new BorderLayout());
		String[] summaryTitles =  new String[] { "序号", "工厂代码", "库存地点", "库存数量", "特殊库存标识"};
		
		DefaultTableModel model = new DefaultTableModel(values, summaryTitles);
		JTable table = new JTable(model) {
			// 重写方法，是单元格不能编辑
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};		
		table.getTableHeader().setReorderingAllowed(false);  
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scrollPane = new JScrollPane(table);
		sapInfo.setPreferredSize(new Dimension(708,240));
		table.getColumnModel().getColumn(0).setPreferredWidth(48);
		table.getColumnModel().getColumn(1).setPreferredWidth(165);
		table.getColumnModel().getColumn(2).setPreferredWidth(165);
		table.getColumnModel().getColumn(3).setPreferredWidth(165);
		table.getColumnModel().getColumn(4).setPreferredWidth(165);
		sapInfo.add(scrollPane,BorderLayout.CENTER);
		centerToScreen();
		pack();
	}

}
