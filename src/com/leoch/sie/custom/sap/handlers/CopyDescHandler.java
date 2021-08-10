package com.leoch.sie.custom.sap.handlers;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.utils.MyPerference;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class CopyDescHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			TCComponent com  = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
			if (com == null) {
				MessageBox.post("请选择零组件/零组件版本进行操作！", "提示", MessageBox.INFORMATION);
				return null;
			}
			if (com instanceof TCComponentBOMLine) {
				TCComponentBOMLine bomLine = (TCComponentBOMLine) com;
				com = bomLine.getItemRevision();
			}
			if (!(com != null &&(com instanceof TCComponentItem || com instanceof TCComponentItemRevision))) {
				MessageBox.post("请选择零组件/零组件版本进行操作！", "提示", MessageBox.INFORMATION);
				return null;
			}
			String[] properties = MyPerference.getCopyProperty();
			String msg = "";
			for (int i = 0; i < properties.length; i++) {
				msg = msg+com.getProperty(properties[i])+"+";
			}
			System.out.println(msg.substring(0, msg.length()-1));
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard(); 
			Transferable tText = new StringSelection(msg); 
			clip.setContents(tText, null);
		} catch (Exception e) {
			MessageBox.post(e);
		}
		return null;
	}

}
