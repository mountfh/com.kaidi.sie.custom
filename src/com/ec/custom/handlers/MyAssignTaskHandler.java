package com.ec.custom.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.util.MessageBox;

public class MyAssignTaskHandler  extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException {
		
		final TCComponent target = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		
//		SwingUtilities.invokeLater(new MyAssignTaskDialog((TCComponentTask) target));
//		SwingUtilities.invokeLater(new MyAdhocDialog((TCComponentTask) target));
		System.out.println(target.getType());
		try {
			new Thread(){
				public void run(){
					TCComponentTask rootTask = null;
					try {
						rootTask = (TCComponentTask) target.getReferenceProperty("root_task");
						MyAdhocDialog ad = new MyAdhocDialog(rootTask);
						ad.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
						MessageBox.post(e.toString(), "´íÎó", MessageBox.ERROR);
					}
				}
			}.start();
//			SwingUtilities.invokeLater(new MyAdhocDialog(rootTask));
//			SwingUtilities.invokeLater(new MyAssignTaskDialog((TCComponentTask) target));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}

