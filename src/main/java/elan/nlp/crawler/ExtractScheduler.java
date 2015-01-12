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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import elan.nlp.crawler.news.DateParser;
import elan.nlp.crawler.news.News;
import elan.nlp.util.ConfigUtil;
import elan.nlp.util.FileUtil;


/**
 * @author elan
 * @create 2013-4-11下午10:13:37
 */
public class ExtractScheduler{

	private static HashMap<String, String> todoList;
	private static List<File> folders = new ArrayList<File>();
	
	private static void init(String path) {
		System.out.println("start preparing todo list....");
		todoList = new HashMap<String, String>();
		
		File[] files = (new File(path)).listFiles();
		for (File file : files) {
			String filename = file.getName();
			String filename_without_sufix=filename.substring(0, filename.indexOf("."));
			
			System.out.println("file shoud end with .txt");
			if (filename.startsWith(".") || !filename.endsWith(".txt")) continue;
			
			// create a folder to accommodate files
			File new_folder = new File(ConfigUtil.getValue("Fulltext_Dir")+"/"+filename_without_sufix);
			folders.add(new_folder);
			
			if (!new_folder.exists()) {
				new_folder.mkdir();
				System.out.println("folder create success "+new_folder.getAbsolutePath());
			} else if (new_folder.isDirectory()) {
				System.out.println("foler "+filename_without_sufix+" already exists");
				File[] subs = new_folder.listFiles();
				for (File f : subs) {
					f.delete();
				}
				System.out.println("delete all the files in this folder, totally delete "+subs.length+" files");
			} else if (new_folder.isFile()){
				new_folder.delete();
				new_folder.mkdir();
				System.out.println("folder create success "+new_folder.getAbsolutePath());
			}

			// parse file to get the URL list
			String news = filename.substring(0, filename.indexOf("_"));
			try {
				Integer num = 1;
				String line = null;
				BufferedReader reader = new BufferedReader(new FileReader(file));
				while( (line = reader.readLine()) != null) {
					String[] elements = line.split(ConfigUtil.getValue("Delimiter"));
					Date date = DateParser.parse(elements[0], News.valueOf(news));
					if (date == null) continue; // if date format transfer failed, just skip
					
					todoList.put(new_folder.getAbsolutePath()+"/"+DateParser.toString(date)+"_"+(num++)+".txt", elements[2]);
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

	private static boolean crawlErrorFiles() {
		File file = new File("News/error_file.dat");
		if (!file.exists()) return false;

		todoList = new HashMap<String, String>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			String line = null;
			while((line = br.readLine()) != null) {
				String filename = line.split("\t")[0];
				String url = line.split("\t")[1];
				todoList.put(filename, url);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("read from News/error_file.dat, items:"+todoList.size());
		
		return true;
	}

	private static void orderFiles() {
		for (File folder : folders) {
			File[] files = folder.listFiles();
			if (files != null && files.length > 0) {
				int count = files.length;
				for (File file : files) {
					String filename = file.getName().replace(".txt", "");
					String date = filename.split("_")[0];
					Integer order = count - Integer.parseInt(filename.split("_")[1]) + 1;
					
					File newfile = new File(order+"_"+date+".txt");
					file.renameTo(newfile);
				}
			}
		}
	}
	
	public static void start(){
		init(ConfigUtil.getValue("News_Dir")+"/todolist");
		
		do {
			File error_file = new File("News/error_file.dat");
			if (todoList.size() == 0) {
				if (error_file.exists()) {
					error_file.delete();
				}
				break;
			}
			FileWriter error_writer = FileUtil.open(error_file);
			System.out.println("Content extracting start....");

			List<BaseCrawler> list = new ArrayList<BaseCrawler>();
			for(Entry<String, String> entry : todoList.entrySet()) {
				Extractor extractor = new Extractor(entry.getValue().toString(), 
						entry.getKey().toString(), error_writer);
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

			FileUtil.close(error_writer);
		} while (crawlErrorFiles());
		
		System.out.println("Content extracting finished");
		System.out.println("begin to rename files with time order...");
		
		orderFiles();
		System.out.println("rename files finished");
	}


}
