package com.jsdf.bean;

public class MessageHandleBean {
	public static final String PRODUCT_LIST_ONE_CODE = "UPDATEONE";
	public static final String PRODUCT_LIST_GETALL_CODE="GETALL";
	
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
