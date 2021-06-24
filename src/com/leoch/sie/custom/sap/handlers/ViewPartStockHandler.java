package com.leoch.sie.custom.sap.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import com.leoch.sie.custom.sap.action.ViewPartStockAction;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class ViewPartStockHandler extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent e) throws ExecutionException {
		TCComponent tcc = (TCComponent) AIFUtility.getCurrentApplication().getTargetComponent();
		if(!(tcc instanceof TCComponentItemRevision)) {
			MessageBox.post("请选择物料版本进行操作", "提示", MessageBox.INFORMATION);
			return null;
		}
		TCComponentItemRevision part = (TCComponentItemRevision) tcc;
		ViewPartStockAction action = new ViewPartStockAction(part);
		action.excute();
		return null;
	}
	
}
