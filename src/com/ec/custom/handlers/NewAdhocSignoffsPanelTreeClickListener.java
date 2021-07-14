package com.ec.custom.handlers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;

import com.teamcenter.rac.common.GenericUserSelectionPanel;
import com.teamcenter.rac.common.TCTreeNode;
import com.teamcenter.rac.common.organization.OrgObject;
import com.teamcenter.rac.common.organization.OrganizationTree;
import com.teamcenter.rac.common.teamroleusertree.TeamRoleUserTree;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.workflow.commands.adhoc.NewRouteTaskSignoffsPanel;
import com.teamcenter.rac.workflow.commands.adhoc.NewSignoffEditPanel;

public class NewAdhocSignoffsPanelTreeClickListener {
	
	private NewSignoffEditPanel currentPanel;
	private JButton addButton;
	private JButton modifyButton;
	
	public void setTarget(NewRouteTaskSignoffsPanel panel) throws Exception {
		
		Class<?> cls = Class.forName("com.teamcenter.rac.workflow.commands.adhoc.NewAdhocSignoffsPanel");
		
		Field f = cls.getDeclaredField("currentPanel");
		f.setAccessible(true);
		currentPanel = (NewSignoffEditPanel) f.get(panel);
		
		f = cls.getDeclaredField("addButton");
		f.setAccessible(true);
		addButton = (JButton) f.get(panel);
		
		f = cls.getDeclaredField("modifyButton");
		f.setAccessible(true);
		modifyButton = (JButton) f.get(panel);
		
		initListener();
		
	}
	
	public void initListener() throws Exception {
		
		Field field = currentPanel.getClass().getDeclaredField("genericUserSelectionPanel");
		field.setAccessible(true);
		GenericUserSelectionPanel genericUserSelectionPanel = (GenericUserSelectionPanel) field.get(currentPanel);
		OrganizationTree tree = genericUserSelectionPanel.getOrgUserSelectionPanel().getOrgTreePanel().getOrgTree();
		TeamRoleUserTree roleUserTree = genericUserSelectionPanel.getProjectTeamSelectionPanel().getTeamRoleUserTree();
		
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					doubleClickTreeNodeAction(e);
				}
			}
		});	
		
		roleUserTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					doubleClickRoleUserNodeAction(e);
				}
			}
		});	
	}
	
	private void doubleClickRoleUserNodeAction(MouseEvent e) {
		
		TeamRoleUserTree tree = (TeamRoleUserTree) e.getSource();
		
		TCTreeNode[] nodes = tree.getSelectedNodes();
		if(nodes == null || nodes.length == 0)return;
		
		Object sectedObject = nodes[0].getUserObject();
		
		if(sectedObject instanceof TCComponentGroupMember) {
			if(addButton != null && addButton.isVisible() && addButton.isEnabled()) {
				addButton.doClick();
			}else if(modifyButton != null && modifyButton.isVisible() && modifyButton.isEnabled()) {
				modifyButton.doClick();
			}
		}
	}
	
	private void doubleClickTreeNodeAction(MouseEvent e) {
		
		OrganizationTree tree = (OrganizationTree) e.getSource();
		OrgObject sectedObject = tree.getSelectedObject();
		
		TCComponent selectedComponent = sectedObject.getComponent();
		
		if("GroupMember".equals(selectedComponent.getType())) {
			if(addButton != null && addButton.isVisible() && addButton.isEnabled()) {
				addButton.doClick();
			}else if(modifyButton != null && modifyButton.isVisible() && modifyButton.isEnabled()) {
				modifyButton.doClick();
			}
		}
	}

}
