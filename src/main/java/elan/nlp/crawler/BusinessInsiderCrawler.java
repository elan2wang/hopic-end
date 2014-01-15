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

public class BusinessInsiderCrawler extends BaseCrawler{

	private static final Logger logger = LoggerFactory.getLogger(BBCCrawler.class);

	private Integer page = 1;
	private String query = "Edward Snowden";
	private String sort = "date";

	// query parameter
	QueryParameters params = null;

	public BusinessInsiderCrawler(String url, String path) {
		super(url, path);
		params = new QueryParameters();
		params.addParameter("page", this.page);
		params.addParameter("q", this.query);
		params.addParameter("sort", sort);
	}

	public void run() {
		System.out.println("BusinessInsider Crawler Start");
		
		// open output file
		FileWriter fw = openOutputFile(path);

		// request for result
		Response response = client.get(url, params.toArray());
		Document doc = Jsoup.parse(response.asString());

		while(true) {
			logger.debug("parsing page "+ page +" ...");
			System.out.print("*");
			// parse doc to get news item
			Elements results = doc.getElementsByClass("search-result");
			for (Element item : results) {
				Element ele = item.getElementsByTag("h3").first().child(0);
				String title = ele.html();
				String url = ele.attr("href");
				String date = item.getElementsByClass("date").first().html();

				// append to output file
				itemAppend(fw, date, title, url);
			}

			// request for next page if possible
			Element next = doc.getElementsByClass("next").first();
			if (next.child(0).attr("href").equals("#") || next.child(0).attr("href").equals("")) {
				break;
			} else {
				page ++;
				params.addParameter("page", this.page);
				response = client.get(url, params.toArray());
				doc = Jsoup.parse(response.asString());
			}
		}

		// close output file
		closeOutputFile(fw);
		System.out.println("BusinessInsider Crawler Finished");
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
