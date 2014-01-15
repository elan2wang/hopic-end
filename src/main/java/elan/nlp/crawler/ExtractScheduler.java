/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elan.nlp.crawler.news.DateParser;
import elan.nlp.crawler.news.News;
import elan.nlp.util.ConfigUtil;


/**
 * @author elan
 * @create 2013-4-11下午10:13:37
 */
public class ExtractScheduler{
	private static final Logger logger = LoggerFactory.getLogger(BBCCrawler.class);
	
	private static HashMap<String, String> todoList;
	
	private static void init(String path) {
		System.out.println("start preparing todo list....");
		todoList = new HashMap<String, String>();
		File dir = new File(path);
		File[] files = dir.listFiles();
		for (File file : files) {
			String filename = file.getName();
			if (filename.startsWith(".") || !filename.endsWith(".dat")) continue;
			
			// create filefolder
			File news_folder = new File(ConfigUtil.getValue("Fulltext_Dir")+"/"
					+filename.substring(0, filename.indexOf(".")));
			if (news_folder.exists()) {
				news_folder.delete();
			}
			if (news_folder.mkdir() == false) {
				logger.error("create folder:"+filename.indexOf(".")+" failed");
				continue;
			}
			System.out.println("folder create success "+news_folder.getAbsolutePath());
			
			// pase file
			String news = filename.substring(0, filename.indexOf("_"));
			try {
				Integer num = 1;
				String line = null;
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while( (line = reader.readLine()) != null) {
					String[] elements = line.split(ConfigUtil.getValue("Delimiter"));
					Date date = DateParser.parse(elements[0], News.valueOf(news));
					if (date == null) continue; // if date format transfer failed, just skip
					
					todoList.put(news_folder.getName()+"/"+DateParser.toString(date)+"_"+(num++)+".txt", elements[2]);
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Totally Count of Items: " + todoList.size());
	}

	public static void start() throws IOException{
		init(ConfigUtil.getValue("News_Dir"));
		if (todoList.size() == 0) return;
		
		System.out.println("Content extracting start....");
		
		List<BaseCrawler> list = new ArrayList<BaseCrawler>();
		for(Entry<String, String> entry : todoList.entrySet()) {
			Extractor extractor = new Extractor(entry.getValue().toString(), 
					ConfigUtil.getValue("Fulltext_Dir")+"/"+entry.getKey().toString());
			list.add(extractor);
		}
		
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(10, 20, 60,  
                TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(list.size()),  
                new ThreadPoolExecutor.CallerRunsPolicy());  
		for(BaseCrawler c : list){
			threadPool.execute(c);
		}
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Content extracting finished");
	}
	
	
}
