package com.jsdf.utils;

import java.util.ArrayList;
import java.util.List;

import com.jsdf.bean.EmailBean;

public class CacheEmail {
	private static List<EmailBean> bean = new ArrayList<EmailBean>();
	
	public static  void clearCache(){
		bean.clear();
	}
	
	public static  List<EmailBean> getCache(){
		return bean;
	}
	
	public static void putEmail(String email,String emailMsg,String emailContext,String Order_id){
		bean.add(new EmailBean(email,emailMsg,emailContext,Order_id));
	}
			
}


