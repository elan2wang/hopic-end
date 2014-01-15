/**
 * Copyright (c) 2013-2015, Jian Wang
 * All rights reserved.
 * 
 * shohokh@gmail.com
 */
package elan.nlp.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import elan.nlp.util.ConfigUtil;

/**
 * @author elan
 * @create 2013-4-9上午10:47:02
 */
public class CrawlScheduler {
	
	public static void start() throws IOException{
		System.out.println("News Crawling Start....");
    	List<BaseCrawler> list = new ArrayList<BaseCrawler>();
    	list.add(new GuardianCrawler(ConfigUtil.getValue("Guardian_Search_API"), "News/Guardian_Obamacare.dat", "Obamacare"));
		//list.add(new BBCCrawler(ConfigUtil.getValue("BBC_Search_URL"), "News/BBC.txt"));
    	//list.add(new BusinessInsiderCrawler(ConfigUtil.getValue("BusinessInsider_Search_URL"), "News/BusinessInsider.txt"));
    	//list.add(new ReutersCrawler(ConfigUtil.getValue("Reuters_Search_URL"), "News/Reuters.txt"));
    	//list.add(new NYTimeCrawler(ConfigUtil.getValue("NYTime_Search_API"), "News/NYTime.txt"));
    	
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
		System.out.println("News Crawling finished");
	}
}
