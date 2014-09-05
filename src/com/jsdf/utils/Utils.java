package com.jsdf.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.jsdf.exception.AppException;

public class Utils {
	private static Properties properties = new Properties();
	public static final String SESSIONID="SESSIONID";
	public  static final String USERNAME="USERNAME";
	public  static final String PASSWORD="PASSWORD";
	public  static final String AUTOLOGIN="AUTOLOGIN";
	public static final String  REMEBERME="REMEBERME";
	
	static{
		InputStream inP = Utils.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			properties.load(inP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Properties getInstanse() throws AppException{
		if(properties==null){
			InputStream inP = Utils.class.getClassLoader().getResourceAsStream("config.properties");
			properties  = new Properties();
			try {
				properties.load(inP);
			} catch (IOException e) {
				throw new AppException("¶ÁÈ¡ÅäÖÃÎÄµµÊ§°Üconfig ", e);
			}
		}
		return properties;
	}
	
	public static String getProperties(String key) throws AppException{
		properties = getInstanse() ;
		return properties.getProperty(key);
	}
	
	public static void setProperties(String key,String value) throws AppException{
		properties = getInstanse();
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
