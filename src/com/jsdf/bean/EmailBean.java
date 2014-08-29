package com.jsdf.bean;

public class EmailBean{
	private String email;
	private String emailMsg;
	private String emailContext;
	private String Order_id;
	public EmailBean(){
		super();
	}
	
	public EmailBean(String email,String emailMsg,String emailContext,String Order_id){
		this.email = email;
		this.emailMsg=emailMsg;
		this.emailContext=emailContext;
		this.Order_id=Order_id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmailMsg() {
		return emailMsg;
	}
	public void setEmailMsg(String emailMsg) {
		this.emailMsg = emailMsg;
	}
	public String getEmailContext() {
		return emailContext;
	}
	public void setEmailContext(String emailContext) {
		this.emailContext = emailContext;
	}
	public String getOrder_id() {
		return Order_id;
	}
	public void setOrder_id(String order_id) {
		Order_id = order_id;
	}
	
	
}