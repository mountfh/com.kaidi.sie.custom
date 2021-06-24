package com.leoch.sie.custom.utils;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCQueryClause;
import com.teamcenter.rac.kernel.TCSession;

public class MyQueryManager {
	
	public static final String QUERY_TYPE_NOMAL = "常规...";
	public static final String QUERY_TYPE_ITEM = "零组件...";
	public static final String QUERY_TYPE_ITEM_ID = "零组件 ID";
	public static final String QUERY_DOCUMENT_ID ="理士_文档";
	public static final String QUERY_DESIGN_ID ="理士_图纸对象查询";
	private TCSession session;
	private String queryType;
	private TCComponent[] aTCComponent;
	
	private TCComponentQueryType imancomponentquerytype;
	private TCComponentQuery imancomponentquery;
	
	private boolean isInit = false;
	
	public MyQueryManager(String queryType, TCSession session){
		
		this.queryType = queryType;
		this.session = session;
		init();
	}
	
	private boolean init(){
		
		try {
			imancomponentquerytype = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
			imancomponentquery = (TCComponentQuery) imancomponentquerytype.find(queryType);
			if(imancomponentquery != null){
				isInit = true;
			}
		} catch (TCException e) {
			return false;
		}
		//加载搜索器
		
		return true;
	}
	
	/**
	 * 
	 * @return 搜索器的所有字段
	 * @throws TCException
	 */
	public TCQueryClause[] getClause() throws TCException{
		TCQueryClause[] qc = null;
		qc = imancomponentquery.describe();
		return qc;
	}
	
	public TCComponentQuery getQueryComponent(){
		return imancomponentquery;
	}
	
	public boolean isInit(){
		return isInit;
	}
	
	public TCComponent[] runQuery(String queryPropName, String queryValue){
		//初始化搜索属性
		String as[] = { queryPropName };
		//搜索值
		String as1[] = { queryValue };
		
		try {
			aTCComponent = imancomponentquery.execute(as, as1);
			return aTCComponent;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
		
	}
	
	public TCComponent[] runQuery(String[] queryPropNames, String[] queryValues){
	
		try {
			aTCComponent = imancomponentquery.execute(queryPropNames, queryValues);			
			return aTCComponent;
		} catch (Exception exception) {
			return null;
		}
		
	}
	
	public TCComponent[] getResult(){
		
		return aTCComponent;
		
	}

}
