package com.ec.custom.handlers;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;

import javax.swing.JButton;

import com.teamcenter.rac.common.GenericUserSelectionPanel;
import com.teamcenter.rac.common.TCTree;
import com.teamcenter.rac.common.organization.OrgObject;
import com.teamcenter.rac.common.organization.OrganizationTree;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.workflow.commands.adhoc.SignoffEditPanel;

public class SignoffEditPanelClickListener {
	
	private SignoffEditPanel currentPanel;
	private JButton addButton;
	private JButton modifyButton;
	private TCTree reviewerTree;
	
	public void addTarget(SignoffEditPanel currentPanel, JButton addButton, JButton modifyButton, TCTree reviewerTree) throws Exception {
		
		this.currentPanel = currentPanel;
		this.addButton = addButton;
		this.modifyButton = modifyButton;
		this.reviewerTree = reviewerTree;
		
		Field field = this.currentPanel.getClass().getDeclaredField("genericUserSelectionPanel");
		field.setAccessible(true);
		GenericUserSelectionPanel genericUserSelectionPanel = (GenericUserSelectionPanel) field.get(currentPanel);
		OrganizationTree tree = genericUserSelectionPanel.getOrgUserSelectionPanel().getOrgTreePanel().getOrgTree();
		
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.getClickCount() == 2) {
					doubleClickTreeNodeAction(e);
				}
			}
		});	
	}
	
	private void doubleClickTreeNodeAction(MouseEvent e) {
		
		OrganizationTree orgTree = (OrganizationTree) e.getSource();
		OrgObject sectedObject = orgTree.getSelectedObject();
		
		TCComponent selectedComponent = sectedObject.getComponent();
		
		boolean enableButton = true;
		if(reviewerTree != null) {
			
			String path = reviewerTree.getSelectionPath().toString().replace("[", "").replace("]", "");
			//首先项配置某些用户不允许更改			
//			for (String s : MyNewProcessDialog.UNABLE_MODIFY_REVIEWER) {
//				if(path.startsWith(s)) {
//					enableButton = false;
//					break;
//				}
//			}
		}
		
		if("GroupMember".equals(selectedComponent.getType())) {
			if(addButton != null && addButton.isVisible() && addButton.isEnabled() && enableButton) {
				addButton.doClick();
			}else if(modifyButton != null && modifyButton.isVisible() && modifyButton.isEnabled() && enableButton) {
				modifyButton.doClick();
			}
		}
	}

}
