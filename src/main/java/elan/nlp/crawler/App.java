package elan.nlp.crawler;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App 
{
	private static final Logger logger = LoggerFactory.getLogger(App.class);

	public static void main( String[] args ) throws IOException
	{
		logger.debug("start app");

		CrawlScheduler.start();
		//ExtractScheduler.start();
	}
}
