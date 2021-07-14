package com.ec.custom.handlers;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.teamcenter.rac.common.TCTypeRenderer;

public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -841579277694610242L;
	private MyTreeNode myTreeNode;
	/**
	 * 重写父类DefaultTreeCellRenderer的方法
	*/
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
	     boolean selected, boolean expanded, boolean isLeaf, int row,boolean hasFocus) {
		  
		// 选中
		if (selected){
			setForeground(getTextSelectionColor());
		} else {
			setForeground(getTextNonSelectionColor());
		}
		// TreeNode
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
		Object obj = treeNode.getUserObject();
		if (obj instanceof MyTreeNode) {
			myTreeNode = (MyTreeNode) obj;
			DefaultTreeCellRenderer tempCellRenderer = new DefaultTreeCellRenderer();
//			tempCellRenderer.setLeafIcon(new ImageIcon(node.getImagePath()));
			return tempCellRenderer.getTreeCellRendererComponent(tree, myTreeNode.toString(), selected, expanded, true, row, hasFocus);
		}
		return super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, hasFocus);
	}	
	
	@Override
	public Icon getDefaultOpenIcon() {
		return TCTypeRenderer.getIcon(myTreeNode.getObject());
    }

	@Override
    public Icon getDefaultClosedIcon() {
		return TCTypeRenderer.getIcon(myTreeNode.getObject());
    }

   
	@Override
    public Icon getDefaultLeafIcon() {
		return TCTypeRenderer.getIcon(myTreeNode.getObject());
    }
	
	@Override
	public Icon getOpenIcon() {
		return TCTypeRenderer.getIcon(myTreeNode.getObject());
	}
	
	@Override
	public Icon getClosedIcon() {
		return TCTypeRenderer.getIcon(myTreeNode.getObject());
	}
	
	@Override
	public Icon getLeafIcon() {
		return TCTypeRenderer.getIcon(myTreeNode.getObject());
	}

}
