package com.jsdf.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {
	private static Properties properties = new Properties();
	public static final String SESSIONID="SESSIONID";
	
	static{
		InputStream inP = Utils.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			properties.load(inP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getProperties(String key){
		return properties.getProperty(key);
	}
	
	public static void setProperties(String key,String value){
		properties.setProperty(key, value);
	}
	
	public static String[] clearEmptyArray(String[] ags){
		int count =0;
		for(String sr : ags){
			if(sr!=null && sr.trim().length()>0){
				count++;
			}
		}
		String[] temp = new String[count];
		int count2=0;
		for(String sr : ags){
			if(sr!=null && sr.trim().length()>0){
				temp[count2]=sr;
				count2++;
			}
		}
		return temp;
	}
}
