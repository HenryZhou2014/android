package com.jsdf.bean;

public class MessageHandleBean {
	public static final String PRODUCT_LIST_ONE_CODE = "UPDATEONE";
	public static final String PRODUCT_LIST_GETALL_CODE="GETALL";
	public static final String PRODUCT_SENDEMAIL_CODE="SENDEMAIL";
	public static final String PRODUCT_SYCNALL_CODE="SYCNALL";
	public static final String EXCEPTION_CODE="EXCEPTION";
	public static final String RELOGIN_CODE="RELOGIN";
	private String msgType;
	private Object data;
	
	public MessageHandleBean(){}
	
	public MessageHandleBean(String msgType,Object data){
		this.msgType = msgType;
		this.data = data;
	}
	
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
	
}
