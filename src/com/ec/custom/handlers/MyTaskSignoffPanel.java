package com.ec.custom.handlers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.workflow.commands.adhoc.AdhocSignoffsPanel;
import com.teamcenter.rac.workflow.commands.adhoc.NewRouteTaskSignoffsPanel;
import com.teamcenter.rac.workflow.commands.adhoc.RouteTaskSignoffsPanel;

public class MyTaskSignoffPanel extends JPanel {

	private static final long serialVersionUID = -5099150274600842021L;

	private NewRouteTaskSignoffsPanel newSignoffPanel;

	private AdhocSignoffsPanel signoffPanel;
	
	private TCSession session;
	
	private JCheckBox adhocDoneCheckBox;
	
	private boolean isRouteTask = false;
	private boolean isNewRouteTask = false;
	
	public MyTaskSignoffPanel(TCComponentTask task) throws Exception {
		
		this.signoffPanel = null;
		String str = null;
		try {
			str = task.getTaskType();
		} catch (Exception localException) {
		}
		
		isRouteTask = str != null && str.equalsIgnoreCase("EPMRouteTask");
		if(isRouteTask)	{
			String preferenceValue = null;
			if (this.session != null) {
				TCPreferenceService  preferenceService = session.getPreferenceService();
				preferenceValue = ((TCPreferenceService) preferenceService).getStringValue("WORKFLOW_new_route_task_panel");
			}
			
			isNewRouteTask = !(preferenceValue != null && preferenceValue.length() != 0 && preferenceValue.trim().toLowerCase().equals("off"));
			if (isNewRouteTask) {
				newSignoffPanel = new NewRouteTaskSignoffsPanel(AIFDesktop.getActiveDesktop(), task);
				newSignoffPanel.setSplitPanelDivider(0.6D);
			} else {
				signoffPanel = new RouteTaskSignoffsPanel(AIFDesktop.getActiveDesktop(), task);
				signoffPanel.setSplitPanelDivider(0.6D);
			}
		} else {
			signoffPanel = new AdhocSignoffsPanel(AIFDesktop.getActiveDesktop(), task);
		}
		JButton btn_OK = null;
		JButton btn_Cancel = null;
		
		if (isRouteTask && isNewRouteTask) {
			btn_OK = newSignoffPanel.getOkButton();
			btn_Cancel = newSignoffPanel.getCancelButton();
			Class<?> cls = Class.forName("com.teamcenter.rac.workflow.commands.adhoc.NewAdhocSignoffsPanel");
			Field f = cls.getDeclaredField("adhocDoneCheckBox");
			f.setAccessible(true);
			adhocDoneCheckBox = (JCheckBox)f.get(newSignoffPanel);
			adhocDoneCheckBox.setSelected(true);
			
		} else {
			btn_OK = signoffPanel.getOkButton();
			btn_Cancel = signoffPanel.getCancelButton();
			Field f = signoffPanel.getClass().getDeclaredField("adhocDoneCheckBox");
			f.setAccessible(true);
			adhocDoneCheckBox = (JCheckBox)f.get(signoffPanel);
			adhocDoneCheckBox.setSelected(true);
		}
		btn_OK.setVisible(false);
		btn_Cancel.setVisible(false);
		
		setLayout(new VerticalLayout(7, 4, 4, 4, 4));
		JLabel localJLabel = new JLabel("");
		add("top.nobind.left", localJLabel);
		add("top.bind", new Separator());
		if (isRouteTask && isNewRouteTask){
			add("unbound.bind.left", newSignoffPanel);
		}else{
			add("unbound.bind.left", signoffPanel);
		}
		
	}
	
	public JCheckBox getAdhocDoneCheckBox(){
		return adhocDoneCheckBox;
	}
	
	public AdhocSignoffsPanel getSignoffPanel() {
		return signoffPanel;
	}
	
	public NewRouteTaskSignoffsPanel getNewSignoffPanel() {
		return newSignoffPanel;
	}
	
	public void doPerformChangeTaskStatusAction() throws Exception{
		
		Class<?> cls = null;
		Object obj = null;
		if(newSignoffPanel != null){
			cls = Class.forName("com.teamcenter.rac.workflow.commands.adhoc.NewAdhocSignoffsPanel");
			obj = newSignoffPanel;
		}else {
			cls = signoffPanel.getClass();
			obj = signoffPanel;
		}
		
		Method mt = cls.getDeclaredMethod("performChangeTaskStatusAction", boolean.class);
//		Method mt = cls.getDeclaredMethod("triggerCompleteAction", boolean.class);
		mt.setAccessible(true);
		//指派审核者时必须要传false，否则不生效
		mt.invoke(obj, false);
	}
}

