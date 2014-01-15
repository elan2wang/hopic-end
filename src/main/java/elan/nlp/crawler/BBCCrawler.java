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


public class BBCCrawler extends BaseCrawler {

	private static final Logger logger = LoggerFactory.getLogger(BBCCrawler.class);

	private Integer page = 1;
	private String query = "US government shutdown";
	private String isText = "on";
	private String search_form = "in-page-search-form";

	// query parameter
	QueryParameters params = null;

	public BBCCrawler(String url, String path) {
		super(url, path);
		params = new QueryParameters();
		params.addParameter("page", this.page);
		params.addParameter("q", this.query);
		params.addParameter("text", this.isText);
		params.addParameter("search_form", this.search_form);
	}

	public void run() {
		System.out.println("BBC Crawler Start");
		
		// open output file
		FileWriter fw = openOutputFile(path);

		// request for result
		Response response = client.get(url, params.toArray());
		Document doc = Jsoup.parse(response.asString());

		while(true) {
			logger.debug("parsing page "+ page +" ...");
			System.out.print("&");
			// parse doc to get news item
			Elements DateItems = doc.getElementsByClass("DateItem");
			for (Element item : DateItems) {
				String date = item.child(0).html();
				Element DateList = item.child(1);
				for (Element ele : DateList.getElementsByClass("title")) {
					String url = ele.attr("href");
					String title = ele.html();
					
					// append to output file
					itemAppend(fw, date, title, url);
				}
			}

			// request for next page if possible
			if (doc.getElementById("next").hasAttr("href")) {
				page ++;
				params.addParameter("page", this.page);
				response = client.get(url, params.toArray());
				doc = Jsoup.parse(response.asString());
			} else {
				break;
			}
		}
		
		// close output file
		closeOutputFile(fw);
		System.out.println("BBC Crawler Finished");
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}
}
