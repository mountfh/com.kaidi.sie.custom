package com.ec.custom.handlers;

import java.net.URL;

import com.teamcenter.rac.kernel.TCComponentTask;

public class MyTreeNode {
	
	private TCComponentTask task = null;
	private String name;
	private URL imageURL = null;
	
	public MyTreeNode(){
		
	}
	
	public MyTreeNode(TCComponentTask task, String name, String imagePath){
		this.task = task;
		this.name = name;
		this.imageURL = MyTreeNode.class.getResource(imagePath);
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public URL getImagePath() {
	    return imageURL;
	}
	public void setImagePath(String imagePath) {
		this.imageURL = MyTreeNode.class.getResource(imagePath);
	}
	
	public TCComponentTask getObject() {
		return task;
	}
	public void setObject(TCComponentTask task) {
		this.task = task;
	}
	
	@Override
	public String toString()
	{
		return name;
	}

}
