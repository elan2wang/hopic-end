/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler.http;

import java.nio.charset.Charset;

/**
 * @author elan
 * @create 2013-4-7下午9:37:55
 */
public class Response {
	
	private int statusCode;
	private byte[] responseAsByteArray = null;
	private String responseAsString = null;
	
	
	public Response(int statusCode, byte[] responseAsByteArray) {
		super();
		this.statusCode = statusCode;
		this.responseAsByteArray = responseAsByteArray;
	}

	public String asString(String charset){
		return new String(responseAsByteArray, Charset.forName(charset));
	}
	
	public String asString(){
		return new String(responseAsByteArray, Charset.forName("ISO-8859-1"));
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public String getResponseAsString() {
		return responseAsString;
	}
	
	public void setResponseAsString(String responseAsString) {
		this.responseAsString = responseAsString;
	}
	
	public byte[] getResponseAsByteArray() {
		return responseAsByteArray;
	}

	public void setResponseAsByteArray(byte[] responseAsByteArray) {
		this.responseAsByteArray = responseAsByteArray;
	}
}
