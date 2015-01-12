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
import elan.nlp.util.FileUtil;

public class NYTimeCrawler extends BaseCrawler {

	private static final Logger logger = LoggerFactory.getLogger(NYTimeCrawler.class);
	private static final SimpleDateFormat dateF = new SimpleDateFormat("yyyyMMdd");
	private static final Integer LIMITS_PER_SEC = 10;
	private static final Integer LIMITS_PER_DAY = 10000;


	private String api_key;
	private String sort = "newest";
	private String begin_date;
	private String end_date;

	private Integer page = 1;
	private Integer page_size = 10;

	private QueryParameters params;

	public NYTimeCrawler(String url, String path, String query) {
		super(url, path, query);
		api_key = ConfigUtil.getValue("NYTime_API_KEY");
		params = new QueryParameters();
		params.addParameter("q", query);
		params.addParameter("api-key", api_key);
		params.addParameter("sort", sort);
	}

	public void run() {
		System.out.println("NYTime Crawler Starting...");
		FileWriter fw = FileUtil.open(path);
		String Delimiter = ConfigUtil.getValue("Delimiter");

		// Crawl data within 100 day, total count are defined by days
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Timestamp(System.currentTimeMillis()));
		calendar.add(Calendar.DAY_OF_YEAR, 1);

		Integer requestCount = 0;
		Boolean isDone = false;
		// because NYTime API don't allow to retrieve data beyond page 100,
		// we need to query within a small period time whose results won't exceed 1000
		// here we just set 30 days
		while (!isDone) {
			
			// calculate begin and end date
			end_date = dateF.format(calendar.getTime());
			calendar.add(Calendar.DAY_OF_YEAR, -30);
			begin_date = dateF.format(calendar.getTime());
			// set parameters
			params.addParameter("begin_date", begin_date);
			params.addParameter("end_date", end_date);
			
			page = 1;
			Integer offset = 0;
			Integer hits = 0;
			do {
				System.out.print("N");
				
				// request for result
				params.addParameter("page", page);
				Response response = client.get(url, params.toArray());

				// if the response is not 200, return
				if (response.getStatusCode() > 200) {
					logger.error(response.getResponseAsString());
					return;
				}

				// API Limits: 10/Second, 10000/Day
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

				logger.debug(requestCount+"");

				// parsing result
				try {
					JSONObject result = new JSONObject(response.asString()).getJSONObject("response");
					offset = result.getJSONObject("meta").getInt("offset");
					hits = result.getJSONObject("meta").getInt("hits");
					// if no result, finished
					if (hits == 0) {
						isDone = true;
						break;
					}
					
					JSONArray docs = result.getJSONArray("docs");
					for (int i=0; i<docs.length(); i++) {
						JSONObject doc = docs.getJSONObject(i);
						JSONObject headline = doc.getJSONObject("headline");
						String title = headline.getString("main");
						String url = doc.getString("web_url");
						String date = doc.getString("pub_date");

						// append to output file
						FileUtil.append(fw, date+Delimiter+title+Delimiter+url+"\n");
					}
				} catch (JSONException e) {
					logger.error("parsing json string to json object failed");
					e.printStackTrace();
				}

				page = page + 1;
			} while(offset+page_size < hits);
		}

		FileUtil.close(fw);
		System.out.println("NYTime Crawler Finished");
	}

}
