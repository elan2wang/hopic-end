/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler.http;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author elan
 * @create 2013-4-7下午2:19:33
 */
public class QueryParameters {
	private static final Logger logger = LoggerFactory.getLogger(QueryParameters.class);
	
	List<QueryParameter> params = new ArrayList<QueryParameter>();
	
	public void addParameter(QueryParameter param){
		int index = params.indexOf(param);
		if(index != -1){
			params.set(index, param);
		}
		params.add(param);
	}
	
	public void addParameter(String name, Object value){
		for (int i=0; i<params.size(); i++) {
			if (params.get(i).getName().equals(name)) {
				logger.debug("parameter [" + name + "] already exist, change value to [" + value + "]");
				params.set(i, new QueryParameter(name, value));
				return;
			}
		}
		// if not exist, add new one
		params.add(new QueryParameter(name, value));
	}
	
	public void removeParameter(String name){
		for (int i=0; i<params.size(); i++) {
			if (params.get(i).getName().equals(name)) {
				params.remove(i);
				return;
			}
		}
	}
	
	public QueryParameter[] toArray(){
		return params.toArray(new QueryParameter[params.size()]);
	}
}
