/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elan.nlp.crawler.http.HttpClient;
import elan.nlp.util.ConfigUtil;

/**
 * @author elan
 * @create 2013-4-7下午9:02:33
 */
public abstract class BaseCrawler implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(BaseCrawler.class);
	
	protected HttpClient client;
	protected String url;
	protected String path;
	protected String query;
	
	public BaseCrawler(){
		super();
		this.client = new HttpClient();
	}

	public BaseCrawler(String url, String path) {
		super();
		this.client = new HttpClient();
		this.url = url;
		this.path = path;
	}

	public BaseCrawler(String url, String path, String query) {
		super();
		this.client = new HttpClient();
		this.url = url;
		this.path = path;
		this.query = query;
	}
	
	public abstract void run();
	
	protected FileWriter openOutputFile(String path) {
		File file = new File(path);
		if(file.exists()) {
			logger.debug(path+" already exists, delete it first");
			file.delete();
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(path);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return fw;
	}

	protected void itemWrite(FileWriter fw, String content){
		try {
			fw.write(content);
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void itemAppend(FileWriter fw, String date, 
			String title, String url){
		itemAppend(fw, date, title, url, ConfigUtil.getValue("Delimiter"));
	}
	
	protected void itemAppend(FileWriter fw, String date, 
			String title, String url, String delimiter) {
		try {
			if (delimiter == null || delimiter.equals("")){
				delimiter = ConfigUtil.getValue("Delimiter");
			}
			fw.append(date+delimiter+title+delimiter+url+"\n");
			fw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void closeOutputFile(FileWriter fw) {
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// getters and setters ========================================
	public HttpClient getClient() {
		return client;
	}

	public void setClient(HttpClient client) {
		this.client = client;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
