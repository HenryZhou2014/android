package com.example.jsdfgrp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jsdf.exception.AppException;
import com.jsdf.http.Httpservice;
import com.jsdf.json.util.JsonUtils;
import com.jsdf.utils.Utils;
import com.jsdf.view.ToastView;

public class MainActivity extends Activity implements OnClickListener {
//	private ImageView back;
	private Button login;
	private Button offline;
	private EditText userName;
	private EditText password;
	private TextView register;
	private MainActivity context;
	private CheckBox remeberMe;
	private CheckBox autoLogin;
	
	private String remeberMeFlag="0";
	private String autoLoginFlag="0";
	
	private String name;
	private String psd;
	private Handler handler;
	public static final int MODLE_VALUE=200;
	public static final String MODLE_NAME="MODLE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		context=this;
	    Resources resource = (Resources) getBaseContext().getResources();
        String usern=resource.getString(R.string.user_name_cannot_be_empty);
        String pass=resource.getString(R.string.password_cannot_be_empty);
        
//        back = (ImageView) findViewById(R.id.login_back);
		login = (Button) findViewById(R.id.login_login);
		offline = (Button) findViewById(R.id.offlineBtn);
		userName = (EditText) findViewById(R.id.login_name);
		password = (EditText) findViewById(R.id.login_password);
		register = (TextView) findViewById(R.id.login_register);
		autoLogin = (CheckBox) findViewById(R.id.checkBoxAutoLogin);
		remeberMe = (CheckBox) findViewById(R.id.checkBoxRemeber);
        register.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        
//        back.setOnClickListener(this);
		login.setOnClickListener(this);
		register.setOnClickListener(this);
		offline.setOnClickListener(this);
		remeberMe.setOnClickListener(this);
		autoLogin.setOnClickListener(this);
		 handler=new Handler(){
		    	public void handleMessage(Message msg){
		    		String[] str = (String[])msg.obj;
          		if("0".equals(str[0])){
          			Intent registerIntent = new Intent(context,ProductListActivity.class);
          			registerIntent.putExtra(MODLE_NAME, "1");
					startActivity(registerIntent);
//					setResult(MODLE_VALUE, registerIntent);
					context.finish();
					try{
						Utils.setProperties(Utils.USERNAME, name);
						Utils.setProperties(Utils.PASSWORD, psd);
						Utils.setProperties(Utils.AUTOLOGIN,autoLoginFlag);
						Utils.setProperties(Utils.REMEBERME,remeberMeFlag);
					}catch(AppException e){
						ToastView toast = new ToastView(context, e.getMessage());
				        toast.setGravity(Gravity.CENTER, 0, 0);
				        toast.show();
					}
	      		}else{
		    		ToastView toast = new ToastView(context, "登录失败："+str[1]);
			        toast.setGravity(Gravity.CENTER, 0, 0);
			        toast.show();
	      		}
		    }
		 };
		 
		 
		 //自动登录
		 try{
			 autoLoginFlag =Utils.getProperties(Utils.AUTOLOGIN);
			 if(autoLoginFlag==null)autoLoginFlag="0";
			 remeberMeFlag = Utils.getProperties(Utils.REMEBERME);
			 if(remeberMeFlag==null)remeberMeFlag="0";
			 if("1".equals(remeberMeFlag)){
				 remeberMe.setChecked(true);
				 name = Utils.getProperties(Utils.USERNAME);
				 psd = Utils.getProperties(Utils.PASSWORD);
				 userName.setText(name);
				 password.setText(psd);
			 }
			 
			 if("1".equals(remeberMeFlag) &&"1".equals(autoLoginFlag)){
				 autoLogin.setChecked(true);
				 login.callOnClick();
			 }
		 }catch(AppException e){
				ToastView toast = new ToastView(context, e.getMessage());
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
		 }
	}
	
	
	
	
	@Override
	public void onClick(View v) {		
        Resources resource = (Resources) getBaseContext().getResources();
        String usern=resource.getString(R.string.user_name_cannot_be_empty);
        String pass=resource.getString(R.string.password_cannot_be_empty);
		Intent intent;
		switch(v.getId()) {
//		case R.id.login_back:
//			break;
		case R.id.login_login:
			name = userName.getText().toString();
			psd = password.getText().toString();
			if("".equals(name)) {				
				ToastView toast = new ToastView(this, usern);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
			} else if("".equals(psd)) {				
				ToastView toast = new ToastView(this, pass);
		        toast.setGravity(Gravity.CENTER, 0, 0);
		        toast.show();
			} else {
				CloseKeyBoard();
				
				new loginSubThread(name, psd,context).start();
//				new Thread(new Runnable(){//主线程中不能直接访问网络，必须通过子线程
//	                @Override
//	                public void run() {
//	                	try {
//	                		String resultStr = Httpservice.LoginGRP(name, psd);
//							String[] code = JsonUtils.getLoginCodeByJsonStr(resultStr);
//						} catch (AppException e) {
//							ToastView toast = new ToastView(context, e.getMessage());
//					        toast.setGravity(Gravity.CENTER, 0, 0);
//					        toast.show();
//						}
//	                	//ToastView toast = new ToastView(MainActivity.this, Httpservice.clientSessionId);
//	    		        //toast.setGravitcy(Gravity.CENTER, 0, 0);
//	    		        //toast.show();
//	                	Log.v("SEESIONID", Httpservice.clientSessionId);
//	                	Utils.setProperties("SESSIONID", Httpservice.clientSessionId);
//	                	Log.v("propertis", Utils.getProperties("SESSIONID"));
//	                }
//	            }).start();
			}
			break;
		case R.id.offlineBtn:
			ToastView toast = new ToastView(this, "启动离线模式");
	        toast.setGravity(Gravity.CENTER, 0, 0);
	        toast.show();
			Intent registerIntent = new Intent(context,ProductListActivity.class);
			registerIntent.putExtra(MODLE_NAME, "2");
//			setResult(MODLE_VALUE, registerIntent);
			startActivity(registerIntent);
			context.finish();
			
			break;
		case R.id.login_register:
			intent = new Intent(this, RegisterActivity.class);
			startActivityForResult(intent, 1);
			
			break;
		case R.id.checkBoxRemeber:
			if(remeberMe.isChecked()){
				remeberMeFlag = "1";
			}else{
				remeberMeFlag = "0";
			}
			Log.v("remeberMeFlag", remeberMeFlag);
			break;
		case R.id.checkBoxAutoLogin:
			if(autoLogin.isChecked()){
				autoLoginFlag = "1";
			}else{
				autoLoginFlag = "0";
			}
			Log.v("autoLogin", autoLoginFlag);
			break;
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	// 关闭键盘
		public void CloseKeyBoard() {
			userName.clearFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(userName.getWindowToken(), 0);
		}
		
	 public Handler getHandler(){
	    	return this.handler;
	    }  
	 
	 
	class loginSubThread extends Thread{
		String name;
		String psd;
		MainActivity context;
		
		public loginSubThread(){
			
		}
		
		public loginSubThread(String name,String psd,MainActivity context){
			this.name = name;
			this.psd = psd;
			this.context = context;
		}
		public void run(){
				Log.v("LOGIN", "STRATING LOGIN...");
				String resultStr ="";
				Message messageObj = messageObj = Message.obtain();
				try {
	              	resultStr = Httpservice.LoginGRP(name, psd);
	              	Log.v("LOGIN", "ENDING LOGIN..." + resultStr);
              	
              		String[] code = JsonUtils.getLoginCodeByJsonStr(resultStr);
	      			messageObj.obj=code;
	      			context.getHandler().sendMessage(messageObj);
				} catch (AppException e) {
					messageObj.obj=new String[]{"001",e.getMessage()};
	      			context.getHandler().sendMessage(messageObj);
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
}
