package com.jsdf.exception;

public class NoMoreDataException extends AppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2370128750621687390L;
	
	public NoMoreDataException(){
		super();
	}
	
	public NoMoreDataException(String msg){
		super(msg);
	}
	
}
