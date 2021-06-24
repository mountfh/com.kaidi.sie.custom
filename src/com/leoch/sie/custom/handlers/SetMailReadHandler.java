package com.leoch.sie.custom.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class SetMailReadHandler extends AbstractHandler {

	String readFlag = "(已 签收)";
	String unreadFlag = "(未 签收)";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		TCComponent mail = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		TCSession session = (TCSession) AIFUtility.getDefaultSession();
		try {
			TCComponent owning_user = mail.getReferenceProperty("owning_user");
			if (!owning_user.equals(session.getUser())) {
				MessageBox.post("无法标记其他人的邮件", "提示", MessageBox.INFORMATION);
				return null;
			}
			String name = mail.getProperty("object_name");
			if (name.endsWith(readFlag)) {
				return null;
			}
			if (name.endsWith(unreadFlag)) {
				name = name.replace(unreadFlag, readFlag);
			} else {
				name = name + readFlag;
			}
			mail.setProperty("object_name", name);
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(e);
		}
		return null;
	}

}
