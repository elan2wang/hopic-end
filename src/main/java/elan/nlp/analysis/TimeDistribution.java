package elan.nlp.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elan.nlp.crawler.news.DateParser;
import elan.nlp.crawler.news.News;
import elan.nlp.util.ConfigUtil;
import elan.nlp.util.FileUtil;

public class TimeDistribution {
	private static final Logger logger = LoggerFactory.getLogger(TimeDistribution.class);

	Map<String, Integer> newsCount;
	
	private News news;
	private String in_path;
	private String out_path;
	
	private Date begin_date;
	private Date end_date;

	public TimeDistribution(News news, String in_path, String out_path) {
		newsCount = new HashMap<String, Integer>();
		this.news = news;
		this.in_path = in_path;
		this.out_path = out_path;
	}

	private void calc() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(new File(in_path)));
			String line = null;
			String dateStr = null;
			Date date = null;
			Boolean firstLine = true;
			while( (line = reader.readLine()) != null) {
				dateStr = line.split(ConfigUtil.getValue("Delimiter"))[0];
				date = DateParser.parse(dateStr, news);
				if (firstLine) {
					end_date = DateParser.parse(dateStr, news);
					firstLine = false;
				}
				
				if (newsCount.containsKey(DateParser.toString(date))) {
					Integer count = newsCount.get(DateParser.toString(date));
					newsCount.put(DateParser.toString(date), count+1);
				} else {
					newsCount.put(DateParser.toString(date), 1);
				}
			}
			logger.debug(DateParser.toString(date));
			begin_date = date;
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void outputTimeDistribution() {
		FileWriter fw = FileUtil.open(out_path);
		Calendar calBegin = Calendar.getInstance();
		Calendar calEnd = Calendar.getInstance();
		calBegin.setTime(begin_date);
		calEnd.setTime(end_date);
		
		while (calBegin.compareTo(calEnd) <= 0) {
			String dateStr = DateParser.toString(calBegin.getTime());
			if (newsCount.containsKey(dateStr)) {
				FileUtil.append(fw, dateStr+", "+newsCount.get(dateStr)+"\n");
			} else {
				FileUtil.append(fw, dateStr+", 0\n");
			}
			calBegin.add(Calendar.DAY_OF_YEAR, 1);
		}
		FileUtil.close(fw);
	}

	public static void main(String args[]) {
		TimeDistribution td = new TimeDistribution(News.Guardian, "News/Guardian_Obamacare.dat", "News/Guardian.txt");
		td.calc();
		td.outputTimeDistribution();
	}
}
