package com.jsdf.json.util;

import android.os.Message;
import android.util.Log;

import com.example.jsdfgrp.ProductListActivity;
import com.jsdf.bean.MessageHandleBean;
import com.jsdf.exception.AppException;
import com.jsdf.http.Httpservice;
import com.jsdf.utils.Utils;

public class LoginThread extends Thread{

	String name;
	String psd;
	ProductListActivity context;
	
	public LoginThread(){
		
	}
	
	public LoginThread(String name,String psd,ProductListActivity context){
		this.name = name;
		this.psd = psd;
		this.context = context;
	}
	public void run(){
			Log.v("LOGIN", "STRATING LOGIN...");
			String resultStr ="";
			Message message  = Message.obtain();
			try {
              	resultStr = Httpservice.LoginGRP(name, psd);
              	Log.v("LOGIN", "ENDING LOGIN..." + resultStr);
          	
          		String[] code = JsonUtils.getLoginCodeByJsonStr(resultStr);
      			
      			message.obj = new MessageHandleBean(MessageHandleBean.RELOGIN_CODE,code);
    	    	context.getHandler().sendMessage(message) ;
			} catch (AppException e) {
				message.obj = new MessageHandleBean(MessageHandleBean.RELOGIN_CODE,new String[]{"001",e.getMessage()});
      			context.getHandler().sendMessage(message);
			}
          	Log.v("SEESIONID", Httpservice.clientSessionId);
          	try{
          		Utils.setProperties("SESSIONID", Httpservice.clientSessionId);
          		Log.v("LOGINED propertis", Utils.getProperties("SESSIONID"));
          	}catch(AppException e){
          		Log.v("LOGINED propertis excetpion ", e.getMessage());
          	}
	}

}
