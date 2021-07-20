package com.leoch.sie.custom.utils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.ICreateInstanceInput;
import com.teamcenter.rac.common.create.SOAGenericCreateHelper;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;
import com.teamcenter.soa.client.model.ErrorStack;
import com.teamcenter.soa.client.model.ServiceData;

public class MyCreateUtil {

		
	public static String handleServiceData(final ServiceData sData) {
		  int noPartErrors = sData.sizeOfPartialErrors();
		  if (noPartErrors > 0) {
		   String errorMessage = "";
		   for (int i = 0; i < noPartErrors; i++) {
		    ErrorStack errorStack = sData.getPartialError(i);
		    String[] messages = errorStack.getMessages();
		    for (int j = 0; messages != null && j < messages.length; j++) {
		     errorMessage = errorMessage + messages[j] + "\n";
		    }
		   }

		   if (errorMessage.length() > 0) {
		    return errorMessage;
		   }
		  }

		  return null;
	}
	
	public static TCComponent createTCComponent(TCSession session, String componentType,
	  Map<String, String> stringProperties, Map<String, BigInteger> intProperties,
	  Map<String, TCComponent> tagProperties) {
	  try {
	   DataManagementService ds = DataManagementService.getService(session);

	   CreateIn in = new CreateIn();
	   in.clientId = componentType + System.currentTimeMillis();

	   CreateInput input = new CreateInput();
	   input.boName = componentType;
	   if (stringProperties != null)
	    input.stringProps = stringProperties;
	   if (intProperties != null)
	    input.intProps = intProperties;
	   if (tagProperties != null)
	    input.tagProps = tagProperties;
	   in.data = input;

	   CreateResponse resp = ds.createObjects(new CreateIn[] { in });
	   String error = handleServiceData(resp.serviceData);
	   if (error == null) {
	    return (TCComponent) resp.serviceData.getCreatedObject(0);
	   } else {
	    System.out.println(error);
	   }
	  } catch (Exception e) {
	   e.printStackTrace();
	  }
	  return null;
	 }
	
	public static TCComponent createWorkspaceObject(String itemTypeName, Map<String, String> propertyMap) {
		  TCSession session = (TCSession) AIFUtility.getDefaultSession();
		  // 获取类型对象
		  IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(session,
		    itemTypeName);
		  // 设置对象属性
		  CreateInstanceInput createInstanceInput = new CreateInstanceInput(createDefinition);
		  if (propertyMap == null) {
		   propertyMap = new HashMap<String, String>();
		  }
		  for (Entry<String, String> entry : propertyMap.entrySet()) {
		   String p = entry.getKey();
		   String v = entry.getValue();
		   createInstanceInput.add(p, v);
		  }
		  // 创建对象
		  ArrayList<CreateInstanceInput> iputList = new ArrayList<>();
		  iputList.add(createInstanceInput);
		  List<ICreateInstanceInput> list = new ArrayList<>(0);
		  list.addAll(iputList);
		  TCComponent comp = null;
		  List<TCComponent> comps = null;
		  try {
		   comps = SOAGenericCreateHelper.create(session, createDefinition, list);
		   comp = (TCComponent) comps.get(0);
		  } catch (TCException e) {
		   e.printStackTrace();
		  }
		  return comp;
	}
}
