package com.jsdf.bean;
import java.util.List;


public class ProductObject {
	private int error;
	private String content;
	private List<OrderList> order_list;
	private int time;
	
	
	public int getError() {
		return error;
	}
	public void setError(int error) {
		this.error = error;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<OrderList> getOrder_list() {
		return order_list;
	}
	public void setOrder_list(List<OrderList> order_list) {
		this.order_list = order_list;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
	
	
}
