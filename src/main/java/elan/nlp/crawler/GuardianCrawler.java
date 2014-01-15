package elan.nlp.crawler;

import java.io.FileWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elan.nlp.crawler.http.QueryParameters;
import elan.nlp.crawler.http.Response;
import elan.nlp.util.ConfigUtil;
import elan.nlp.util.FileUtil;

public class GuardianCrawler extends BaseCrawler{
	private static final Logger logger = LoggerFactory.getLogger(GuardianCrawler.class);
	private static final Integer LIMITS_PER_SEC = 12;
	private static final Integer LIMITS_PER_DAY = 5000;

	private String api_key;
	private String begin_date;
	private String end_date;
	private String sort = "newest";

	private Integer page = 1;
	private Integer page_size = 50;

	private QueryParameters params;

	public GuardianCrawler() {
		super();
		api_key = ConfigUtil.getValue("Guardian_API_KEY");
		params = new QueryParameters();
		params.addParameter("q", query);
		params.addParameter("api-key", api_key);
		params.addParameter("sort", sort);
		params.addParameter("page-size", page_size);
	}

	public GuardianCrawler(String url, String path) {
		super(url, path);
		api_key = ConfigUtil.getValue("Guardian_API_KEY");
		params = new QueryParameters();
		params.addParameter("q", query);
		params.addParameter("api-key", api_key);
		params.addParameter("sort", sort);
		params.addParameter("page-size", page_size);
	}

	public GuardianCrawler(String url, String path, String query) {
		super(url, path, query);
		api_key = ConfigUtil.getValue("Guardian_API_KEY");
		params = new QueryParameters();
		params.addParameter("q", query);
		params.addParameter("api-key", api_key);
		params.addParameter("order-by", sort);
		params.addParameter("page-size", page_size);
	}

	public void run() {
		System.out.println("Guardian Crawler Starting...");
		FileWriter fw = FileUtil.open(path);
		String Delimiter = ConfigUtil.getValue("Delimiter");
		
		Integer requestCount = 0;
		Integer total = 0;
		Integer startIndex = 0;
		
		do {
			// request for result
			params.addParameter("page", page);
			Response response = client.get(url, params.toArray());

			// if the response is not 200, return
			if (response.getStatusCode() > 200) {
				logger.error(response.getResponseAsString());
				return;
			}

			// API Limits: 12/Second, 5000/Day
			requestCount ++;
			if (requestCount % LIMITS_PER_SEC == 0) {
				logger.debug("Need Sleep");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if (requestCount > LIMITS_PER_DAY) {
				logger.info("Request Count Exceed API Limits(10000/Day)");
				return;
			}
			
			// parsing result
			try {
				JSONObject result = new JSONObject(response.asString()).getJSONObject("response");
				total = result.getInt("total");
				startIndex = result.getInt("startIndex");

				JSONArray docs = result.getJSONArray("results");
				for (int i=0; i<docs.length(); i++) {
					JSONObject doc = docs.getJSONObject(i);
					String title = doc.getString("webTitle");
					String url = doc.getString("webUrl");
					String date = doc.getString("webPublicationDate");
					
					// append to output file
					FileUtil.append(fw, date+Delimiter+title+Delimiter+url+"\n");
				}
			} catch (JSONException e) {
				logger.error("parsing json string to json object failed");
				e.printStackTrace();
			}
			
			page = page + 1;
		} while (startIndex + page_size < total);

		FileUtil.close(fw);
		System.out.println("Guardian Crawler Finished...");
	}

	// ~~~~~ Getters and Setters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public String getApi_key() {
		return api_key;
	}

	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public String getBegin_date() {
		return begin_date;
	}

	public void setBegin_date(String begin_date) {
		this.begin_date = begin_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getPage_size() {
		return page_size;
	}

	public void setPage_size(Integer page_size) {
		this.page_size = page_size;
	}	
}
