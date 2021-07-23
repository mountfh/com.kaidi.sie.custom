package com.ec.custom.handlers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.UIUtilities;

public class MyAdhocDialog extends AbstractAIFDialog implements ActionListener{
	
	private static final long serialVersionUID = -3264543276444332123L;

	private TCComponentTask rootTask;
	
	private List<MyTreeNode> tasks;
	
	private JPanel sumPanel;
	
	private JPanel parentPanel;
	
	private Map<TCComponentTask, MyTaskSignoffPanel> signoffPanels = new HashMap<>();

	private JButton btn_OK;

	private JButton btn_Cancel;;
	
	private boolean isClickOk = false;

	private JTree tree;

	private JTextArea text_explain;
	
	public MyAdhocDialog(TCComponentTask rootTask) throws Exception {
		
		super();
		setTitle("指派流程审核者");
		this.rootTask = rootTask;
		
		setModal(true);
		
		parentPanel = new JPanel(new BorderLayout());
		parentPanel.setBorder(new EtchedBorder());
		sumPanel = new JPanel(new BorderLayout());
		
		text_explain = new JTextArea(0,20);
		text_explain.setLineWrap(true);
		text_explain.setBorder(new TitledBorder("说明"));
		JScrollPane tsp = new JScrollPane(text_explain);
		tsp.setPreferredSize(new Dimension(200, 200));
		text_explain.setEditable(false);
		
		resetSumPanel(getFirstTask());
		
		JPanel westPanel = new JPanel(new BorderLayout());
		westPanel.add(BorderLayout.CENTER, getReviewTaskTree());
		westPanel.add(BorderLayout.SOUTH, tsp);
		
		parentPanel.add(BorderLayout.WEST, westPanel);
		parentPanel.add(BorderLayout.CENTER, sumPanel);
		parentPanel.add(BorderLayout.SOUTH, getButtonPanel());	
		tree.setSelectionRow(1);
		
		getContentPane().add(parentPanel);
		getContentPane().revalidate();
		
		UIUtilities.centerToScreen(this, 1.5D, 1.0D, 0.5D, 0.4D);
//		pack();
		setSize(700, 600);
	}
	
	private TCComponentTask getFirstTask() throws Exception{
		System.out.println();
		 List<MyTreeNode> temp = getReviewTaskNodes();
		 if(temp.size()<1) {
			 throw new Exception("流程中没有审核任务，无需指派！");
		 }
		return temp.get(0).getObject();
	}
	
	private JPanel initializePanel(TCComponentTask task) throws Exception {
		
		if(task == null)return new JPanel();
		
		MyTaskSignoffPanel panel = null;
		if(signoffPanels.containsKey(task)){
			panel = signoffPanels.get(task);
		}else{
			panel = new MyTaskSignoffPanel(task);
			signoffPanels.put(task, panel);
		}
		
		if(panel.getSignoffPanel() != null) {
			new AdhocSignoffsPanelTreeClickListener().setTarget(panel.getSignoffPanel());
		}
		if(panel.getNewSignoffPanel() != null) {
			new NewAdhocSignoffsPanelTreeClickListener().setTarget(panel.getNewSignoffPanel());
		}
		
		return panel;
	}
	
	public void resetSumPanel(TCComponentTask task) throws Exception{
		
		sumPanel.removeAll();
		sumPanel.add(BorderLayout.CENTER, initializePanel(task));
		text_explain.setText(getTaskDesc(task));
		sumPanel.updateUI();
		setSize(920, 575);
		setPersistentDisplay(true);
	}
	
	public JPanel getButtonPanel(){
		
		btn_OK = new JButton("确定");
		btn_Cancel = new JButton("关闭");
		btn_OK.addActionListener(this);
		btn_Cancel.addActionListener(this);
		JPanel panel = new JPanel();
		panel.add(btn_OK);
		panel.add(btn_Cancel);
		return panel;
	}
	
	public JTree getReviewTaskTree() throws Exception{
		
		MyTreeNode tn = new MyTreeNode(null, rootTask.toString(), "/resources/processdesignerapplication_16.png");
		DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(tn);
		
		List<MyTreeNode> tasks = getReviewTaskNodes();
//		try {
//			tasks.sort(new MyTreeNodeComparetor());
//		}catch(Exception e) {
//			e.printStackTrace();
//		}
		
		MyTreeNode[] nodes = sortTreeNode(tasks);
		
		for (MyTreeNode task : nodes) {
			topNode.add(new DefaultMutableTreeNode(task));
		}	
		tree = new JTree(topNode);
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				Object obj = node.getUserObject();
				if(obj instanceof MyTreeNode) {
					try {
						TCComponentTask task = ((MyTreeNode) obj).getObject();
						resetSumPanel(task);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		tree.setCellRenderer(new MyTreeCellRenderer());
		
		return tree;
		
	}
	
	public String getTaskDesc(TCComponentTask task) throws TCException{
		if(task == null)return "";
		String type = task.getTaskType();
		if("EPMSelectSignoffTask".equals(type) || "EPMPerformSignoffTask".equals(type)) {
			return task.getParent().getProperty("object_desc");
		}
		return task.getProperty("object_desc");
	}
	
	public List<MyTreeNode> getReviewTaskNodes() throws Exception{
		
		if(tasks != null)return tasks;
		
		tasks = new ArrayList<>();
		
		List<TCComponentTask> children = getChildrenTask(rootTask);
		
		for (TCComponentTask child : children) {
			String taskType = child.getTaskType();
			if("EPMReviewTask".equals(taskType)){
				tasks.add(new MyTreeNode(child.getSubtasks()[0], child.toString(), "/resources/reviewtask_16.png"));
			}else if("EPMAcknowledgeTask".equals(taskType)){
				tasks.add(new MyTreeNode(child.getSubtasks()[0], child.toString(), "/resources/acknowledgetask_16.png"));
			}else if("EPMRouteTask".equals(taskType)){
				tasks.add(new MyTreeNode(child, child.toString(), "/resources/routetask_16.png"));
			}
		}
		return tasks;
	}
	
	private List<TCComponentTask> getChildrenTask(TCComponentTask parentTask) throws TCException{
		
		TCComponent[] ps = parentTask.getReferenceListProperty("child_tasks");
		List<TCComponentTask> ls = new ArrayList<>(ps.length);
		for (TCComponent p : ps) {
			if(p instanceof TCComponentTask)ls.add((TCComponentTask) p);
		}
		return ls;
	}
	
	
	/**
	 * TODO 判断是否所有的任务都指派的审核人
	 * @return
	 * @throws Exception
	 */
	public boolean isAllTaskHasReviewer() throws Exception {
		
		for (MyTreeNode treeNode : getReviewTaskNodes()) {
			
			TCComponentTask task = treeNode.getObject();
			
			if(task.getTaskType().equalsIgnoreCase("EPMRouteTask") && !isRountTaskHasReviewer(task)) {
				return false;
			}else if(task.getParent().getTaskType().equalsIgnoreCase("EPMReviewTask") && !isReviewTaskHasReviewer(task.getParent())){
				return false;
			}else if(task.getParent().getTaskType().equalsIgnoreCase("EPMAcknowledgeTask") && !isReviewTaskHasReviewer(task.getParent())){
				return false;
			}
			
		}
		
		return true;
	}
	
	public boolean isReviewTaskHasReviewer(TCComponentTask task) throws Exception{
		TCComponent[] signoffs = task.getReferenceListProperty("valid_signoffs");
		return signoffs != null && signoffs.length > 0;
		
	}
	
	public boolean isRountTaskHasReviewer(TCComponentTask task) throws Exception{
		TCComponentTask[] subTasks = task.getSubtasks();
		for (TCComponentTask st : subTasks) {
			if(isReviewTaskHasReviewer(st))return true;
		}
		return false;
	}
	
	public boolean isClickOK(){
		return isClickOk;
	}
	
	public MyTreeNode[] sortTreeNode(List<MyTreeNode> nodes){
		
		MyTreeNode[] nodeArray = nodes.toArray(new MyTreeNode[nodes.size()]);
		
		for(int i = 0; i < nodeArray.length; i++) {
			for(int j = i+1; j < nodeArray.length; j++) {
				String n1 = nodeArray[i].getName().replace("、", ".");
		    	String n2 = nodeArray[j].getName().replace("、", ".");
		    	
		    	int i1 = Integer.parseInt(n1.contains(".") ? n1.split("[.]")[0] : "0");
		    	int i2 = Integer.parseInt(n2.contains(".") ? n2.split("[.]")[0] : "0");
		    	
		    	if(i1 > i2) {
		    		MyTreeNode temp = nodeArray[i];
		    		nodeArray[i] = nodeArray[j];
		    		nodeArray[j] = temp;
		    	}
			}
		}
		return nodeArray;
		
	}
	
	public class MyTreeNodeComparetor implements Comparator<MyTreeNode> {
	    @Override
	    public int compare(MyTreeNode tn1, MyTreeNode tn2) {
	    	
	    	try {
	    		String n1 = tn1.getName().replace("、", ".");
		    	String n2 = tn2.getName().replace("、", ".");
		    	
		    	int i1 = Integer.parseInt(n1.contains(".") ? n1.split("[.]")[0] : "0");
		    	int i2 = Integer.parseInt(n2.contains(".") ? n2.split("[.]")[0] : "0");
		    	return Integer.compare(i1, i2);
	    	}catch(Exception e) {
	    		e.printStackTrace();
	    		return 0;
	    	}
	    	
	    }
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource().equals(btn_OK)){
			try{
				for (MyTaskSignoffPanel signoffPanel : signoffPanels.values()) {
					signoffPanel.getAdhocDoneCheckBox().setSelected(true);
					signoffPanel.doPerformChangeTaskStatusAction();
				}
				isClickOk = true;
			}catch(Exception e1){
				e1.printStackTrace();
				isClickOk = false;
				MessageBox.post(this, e1.toString(), "错误", MessageBox.ERROR);
			}
			
			dispose();
			
		}else if(e.getSource().equals(btn_Cancel)){
			isClickOk = false;
			dispose();
		}
	}

}
