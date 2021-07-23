package com.custom.bean;

public class ECPartBean {

	public String name;
	public String id;
	public String url;
	public String desc;
	public String filename;
	public String type;
	public String picid;
	public String revid;
	
	public ECPartBean() {}
	
	public ECPartBean(ECPartBean bean) {
		if(bean==null) return;
		id = bean.getId();
		name = bean.getName();
		url = bean.getUrl();
		desc = bean.getDesc();
		filename = bean.getFilename();
		type = bean.getType();
		picid = bean.getPicid();
		revid = bean.getRevid();
		
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPicid() {
		return picid;
	}

	public void setPicid(String picid) {
		this.picid = picid;
	}

	public String getRevid() {
		return revid;
	}

	public void setRevid(String revid) {
		this.revid = revid;
	}
	
	
	
	
	
}
