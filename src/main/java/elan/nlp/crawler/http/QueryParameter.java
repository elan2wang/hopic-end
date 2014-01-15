/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler.http;


/**
 * @author elan
 * @create 2013-4-7下午2:13:45
 */
public class QueryParameter {
	
	private String name;
	private String value;
	
	public QueryParameter(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public QueryParameter(String name, Object value) {
		super();
		this.name = name;
		this.value = value.toString();
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public String toString() {
		return "QueryParameter [name=" + name + ", value=" + value + "]";
	}
	
}
