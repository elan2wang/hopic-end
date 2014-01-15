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

public class ReutersCrawler extends BaseCrawler{

	private static final Logger logger = LoggerFactory.getLogger(ReutersCrawler.class);

	private Integer page = 1;
	private String query = "Edward Snowden";

	// query parameter
	QueryParameters params = null;

	public ReutersCrawler(String url, String path) {
		super(url, path);
		params = new QueryParameters();
		params.addParameter("pn", this.page);
		params.addParameter("blob", this.query);
	}

	public void run() {
		System.out.println("Reuters Crawler Start");
		
		// open output file
		FileWriter fw = openOutputFile(path);

		// request for result
		Response response = client.get(url, params.toArray());
		Document doc = Jsoup.parse(response.asString());

		while(true) {
			logger.debug("parsing page "+ page +" ...");
			System.out.print("+");
			// parse doc to get news item
			Elements DateItems = doc.getElementsByClass("searchResult");
			for (Element item : DateItems) {
				Element ele = item.getElementsByClass("searchHeadline").first().child(0);
				String title = ele.html();
				String url = ele.attr("href");
				String date = item.getElementsByClass("timestamp").first().html();
				
				// append to output file
				itemAppend(fw, date, title, url);

			}

			// request for next page if possible
			page ++;
			params.addParameter("pn", this.page);
			response = client.get(url, params.toArray());
			doc = Jsoup.parse(response.asString());
			if (doc.getElementsByClass("moduleBody").first().html().
					contains("No results were found.")) {
				break;
			}
		}

		// close output file
		closeOutputFile(fw);
		System.out.println("Reuters Crawler Finished");
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
