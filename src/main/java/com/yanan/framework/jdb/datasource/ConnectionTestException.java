package com.yanan.framework.jdb.datasource;

public class ConnectionTestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2856581927259512689L;
	public ConnectionTestException(String id) {
		super("connection test failed at datasource '"+id+"'");
	}
	public ConnectionTestException(String id, Throwable t) {
		super("connection test failed at datasource '"+id+"'",t);
	}

}