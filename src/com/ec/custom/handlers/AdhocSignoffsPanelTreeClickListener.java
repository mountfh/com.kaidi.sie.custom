package com.ec.custom.handlers;

import java.lang.reflect.Field;

import javax.swing.JButton;

import com.teamcenter.rac.common.TCTree;
import com.teamcenter.rac.workflow.commands.adhoc.AdhocSignoffsPanel;
import com.teamcenter.rac.workflow.commands.adhoc.SignoffEditPanel;

public class AdhocSignoffsPanelTreeClickListener {
	
	private SignoffEditPanel currentPanel;
	private JButton addButton;
	private JButton modifyButton;
	
	public void setTarget(AdhocSignoffsPanel panel) throws Exception {
		
		if(panel == null) {
			System.out.println("Пе");
			return;
		}
		Class<?> panelClass = panel.getClass();
		System.out.println(panelClass);
		Field f = panelClass.getDeclaredField("currentPanel");
		f.setAccessible(true);
		currentPanel = (SignoffEditPanel) f.get(panel);
		
		f = panelClass.getDeclaredField("addButton");
		f.setAccessible(true);
		addButton = (JButton) f.get(panel);
		
		f = panelClass.getDeclaredField("modifyButton");
		f.setAccessible(true);
		modifyButton = (JButton) f.get(panel);
		
		TCTree reviewerTree = null;
		try {
			f = panelClass.getDeclaredField("processTreeView");
			f.setAccessible(true);
			reviewerTree = (TCTree) f.get(panel);
		}catch(Exception e) {
			
		}
		
//		new SignoffEditPanelClickListener().add
		new SignoffEditPanelClickListener().addTarget(currentPanel, addButton, modifyButton, reviewerTree);		
		
	}

}
