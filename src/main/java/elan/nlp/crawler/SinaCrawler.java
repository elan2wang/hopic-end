package elan.nlp.crawler;

import java.io.FileWriter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elan.nlp.crawler.http.QueryParameters;
import elan.nlp.crawler.http.Response;


public class SinaCrawler extends BaseCrawler {

	public static final Logger logger = 
			LoggerFactory.getLogger(SinaCrawler.class);

	private Integer	page = 1;
	private String	range = "title";
	private String	type = "news";
	private String	sort = "time";
	private String	query = "复旦投毒案";
	
	
	QueryParameters params = null;

	public SinaCrawler(String url, String path) {
		super(url, path);
		params = new QueryParameters();
		params.addParameter("page", this.page);
		params.addParameter("q", this.query);
		params.addParameter("c", this.type);
		params.addParameter("sort", this.sort);
		params.addParameter("range", this.range);
	}

	@Override
	public void run() {
		logger.info("Sina News Crawler Started...");

		// open output file
		FileWriter fw = openOutputFile(path);

		// request for result
		logger.info(url);
		Response response = client.get(url, params.toArray());
		Document doc = Jsoup.parse(response.asString("gb2312"));
		
		while (true) {
			logger.info("parsing page "+ page +" ...");
			
			Elements items = doc.getElementsByClass("box-result");
			for (Element item : items) {
				String date = item.getElementsByClass("fgray_time").get(0).text();
				String url = item.getElementsByTag("a").get(0).attr("href");
				String title = item.getElementsByTag("a").get(0).text();
				itemAppend(fw, date, title, url);
			}
		
			if (doc.getElementsByAttributeValue("title", "下一页").size() != 0) {
				page++;
				params.addParameter("page", this.page);
				response = client.get(url, params.toArray());
				doc = Jsoup.parse(response.asString("gb2312"));
			} else {
				break;
			}
		}
	}

}
