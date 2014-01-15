/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.util;

import java.io.File;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author elan
 * @create 2013-4-7下午3:41:46
 */
public class ConfigUtil {
	private static final Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
	
	private static Properties defaultProperties;
	
	static {
		init();
		logger.debug("load properties file successfully");
	}
	
	static void init(){
		defaultProperties = new Properties();
		
		String propertyFile = "onto.properties";
		loadProperties(defaultProperties, "."+File.separatorChar+propertyFile);
	}
	
	private static void loadProperties(Properties props, String path){
		try{
			props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static String getValue(String key){
		return defaultProperties.getProperty(key);
	}
	
	public static void updateProperty(String key, String value){
		defaultProperties.setProperty(key, value);
	}
}
