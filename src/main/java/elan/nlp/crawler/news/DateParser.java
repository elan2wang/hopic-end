package elan.nlp.crawler.news;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateParser {
	private static final Logger logger = LoggerFactory.getLogger(DateParser.class);

	// Reuters			EEE MMM dd, yyyy hh:mmaaa z	(Wed Nov 27, 2013 9:15am EST)
	// BBC				d MMMM yyyy					(27 November 2013)
	// BusinessInsider	MMM. d, yyyy, hh:mm a		(Nov. 26, 2013, 6:22 PM)
	// NYTime			yyyy-MM-dd'T'HH:mm:ss'Z'	(2013-11-27T22:24:47Z)
	
	private static final String Reuters_Format = "EEE MMM dd, yyyy hh:mmaaa z";
	private static final String BBC_Format = "d MMMM yyyy";
	private static final String BusinessInsider_Format = "MMM. d, yyyy, hh:mm a";
	private static final String NYTime_Format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String Guardian_Format = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final DateFormat dateF = DateFormat.getDateInstance(DateFormat.LONG, Locale.US);

	/**
	 * @param dateStr the String with date format
	 * @param news news type[Reuters, BBC, BusinessInsider, NYTime]
	 * @return
	 */
	public static Date parse(String dateStr, News news){
		switch(news) {
		case Reuters:
			((SimpleDateFormat) dateF).applyPattern(Reuters_Format);
			break;
		case BBC:
			((SimpleDateFormat) dateF).applyPattern(BBC_Format);
			break;
		case BusinessInsider:
			((SimpleDateFormat) dateF).applyPattern(BusinessInsider_Format);
			break;
		case NYTime:
			((SimpleDateFormat) dateF).applyPattern(NYTime_Format);
			break;
		case Guardian:
			((SimpleDateFormat) dateF).applyPattern(Guardian_Format);
			break;
		}
		
		Date date = null;
		try {
			date = dateF.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return date;
	}

	public static String toString(Date date) {
		SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd");
		
		return df.format(date).toString();
	}
	
	/**
	 * @param begin_date yyyy-MM-dd
	 * @param end_date
	 * @param news
	 * @return
	 */
	public static Integer calc_interval(String begin_date, String end_date, News news) {
		Integer interval = 0;
		
		try {
			((SimpleDateFormat) dateF).applyPattern("yyyy-MM-dd");
			Date beginDate = dateF.parse(begin_date);
			Date endDate = parse(end_date, news);
			interval = (int) ((endDate.getTime()-beginDate.getTime())/(24*3600*1000));
			logger.debug(beginDate.toString()+"--"+endDate.toString()+"--"+interval+"");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return interval;
	}

	public static void main( String[] args ) throws IOException
	{
		logger.info("test DateParser");
		
		logger.info(toString(DateParser.parse("2013-12-23T22:34:22Z", News.NYTime)));
		logger.info(""+DateParser.calc_interval("2013-06-09", "2013-12-23T22:34:22Z", News.NYTime));
	}
}
