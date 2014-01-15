package elan.nlp.crawler;

import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elan.nlp.crawler.http.QueryParameters;
import elan.nlp.crawler.http.Response;
import elan.nlp.util.ConfigUtil;

public class NYTimeCrawler extends BaseCrawler {

	private static final Logger logger = LoggerFactory.getLogger(NYTimeCrawler.class);
	private static final SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMdd");

	private String query = "\"edward*snowden\"";
	private String sort = "newest";

	private String api_key;
	private String begin_date;
	private String end_date;

	private Integer page = 0;
	private Integer days = 204;

	private QueryParameters params;

	public NYTimeCrawler(String url, String path) {
		super(url, path);
		api_key = ConfigUtil.getValue("NYTime_API_KEY");
		params = new QueryParameters();
		params.addParameter("q", query);
		params.addParameter("api-key", api_key);
		params.addParameter("sort", sort);
	}

	public void run() {
		System.out.println("NYTime Crawler Start");
		
		// open output file
		FileWriter fw = openOutputFile(path);

		// Crawl data within each day, total count are defined by days
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Timestamp(System.currentTimeMillis()));

		Integer dayCount = 0;
		Integer requestCount = 0;
		while (dayCount < days) {
			System.out.print("%");
			dayCount++;

			// calculate begin and end date
			end_date = dateF.format(calendar.getTime());
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			begin_date = dateF.format(calendar.getTime());
			// debug
			logger.debug(begin_date + " - " + end_date);

			page = 0;
			Integer offset = 0;
			Integer hits = 0;

			// set parameters
			params.addParameter("begin_date", begin_date);
			params.addParameter("end_date", end_date);
			params.addParameter("page", page);

			do {
				// request for result
				Response response = client.get(url, params.toArray());

				// if the response is not 200, return
				if (response.getStatusCode() > 200) {
					logger.error(response.getResponseAsString());
					return;
				}

				// API Limits: 10/Second, 10000/Day
				requestCount ++;
				if (requestCount % 10 == 0) {
					logger.debug("Need Sleep");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (requestCount > 10000) {
					logger.info("Request Count Exceed API Limits(10000/Day)");
					return;
				}

				logger.debug(requestCount+"");

				// parsing result
				try {
					JSONObject result = new JSONObject(response.asString()).getJSONObject("response");
					offset = result.getJSONObject("meta").getInt("offset");
					hits = result.getJSONObject("meta").getInt("hits");

					JSONArray docs = result.getJSONArray("docs");
					for (int i=0; i<docs.length(); i++) {
						JSONObject doc = docs.getJSONObject(i);
						JSONObject headline = doc.getJSONObject("headline");
						String title = headline.getString("main");
						String url = doc.getString("web_url");
						String date = doc.getString("pub_date");
						
						// append to output file
						itemAppend(fw, date, title, url);
					}
				} catch (JSONException e) {
					logger.error("parsing json string to json object failed");
					e.printStackTrace();
				}

				// get next page
				params.addParameter("page", page++);
			} while(offset+10 < hits);
		}

		// close output file
		closeOutputFile(fw);
		System.out.println("NYTime Crawler Finished");
	}

	// Setters ===================================
	public void setApi_key(String api_key) {
		this.api_key = api_key;
	}

	public void setQuery(String q) {
		this.query = q;
	}

	public void setBegin_date(String begin_date) {
		this.begin_date = begin_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	// getters ==================================
	public String getApi_key() {
		return api_key;
	}

	public String getQuery() {
		return query;
	}

	public String getBegin_date() {
		return begin_date;
	}

	public String getEnd_date() {
		return end_date;
	}
}
