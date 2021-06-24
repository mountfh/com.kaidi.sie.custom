package com.leoch.sie.custom.utils;

public class test {


	public static void main(String[] args) {
		
		try {
			String rgekz = "22:333";
			rgekz = rgekz.substring(0, rgekz.indexOf(":"));
			System.out.println(rgekz);
			rgekz = "22333";
			System.out.println(rgekz.indexOf(":"));
		} catch (Exception e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}

}
