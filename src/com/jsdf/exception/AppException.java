package com.jsdf.exception;

public class AppException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6419524113983800668L;

	public AppException(){
		super();
	}
	
	public AppException(String str){
		super(str);
	}
	
	public AppException(String str,Throwable e){
		super(str,e);
	}
	
	public AppException(Throwable e){
		super(e);
	}
}
