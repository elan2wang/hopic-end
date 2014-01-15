package elan.nlp.entity;

import elan.nlp.crawler.http.HttpClient;
import elan.nlp.crawler.http.QueryParameters;
import elan.nlp.crawler.http.Response;
import elan.nlp.util.ConfigUtil;

public class AlchemyAPI {

    private String _apiKey;
    private String _requestUri;

    private HttpClient client;
    private QueryParameters params;
    
    public AlchemyAPI(String requestUri, String apikey) {
    	_apiKey = apikey;
    	_requestUri = requestUri;
    	
    	client = new HttpClient();
    	params = new QueryParameters();
    	params.addParameter("apikey", _apiKey);
    	params.addParameter("outputMode", "json");
    	params.addParameter("disambiguate", 0);
    }

    public Response GetRankedNamedEntities(String text) {
    	if (null == text || text.length() < 5)
            throw new IllegalArgumentException("Enter some text to analyze.");
    	params.addParameter("text", text);
    	
    	return client.get(_requestUri, params.toArray());
    }
    
    public static void main(String args[]) {
    	String text = "december last update et share page delicious digg facebook reddit stumbleupon twitter email print continue read main story related stories tech firm seek surveillance reform google outrage nsa hacking apple list government datum request intelligence agency be foolish use modern technology allow track terrorist plot chairman parliament intelligence committee have say sir malcolm rifkind comment come internet firm warn us government reform collection bulk datum email call proportionality security individual liberty email be read computer be see human eye us firm google apple facebook twitter aol microsoft linkedin yahoo have form alliance call reform government surveillance group have write letter president obama us congress argue current surveillance practice undermine freedom people tiny number sir malcolm conservative mp chairman intelligence security committee tell bbc radio today program bulk datum be collect do mean people be sit reading everybody email company understand everybody company add issue want address be proportionality onus have be government much agency sir malcolm say be benefit be achieve battle terrorism such significance justify intrusion privacy people read email email be process computer identify tiny number correlate terrorist move internet firm come whistle-blower edward snowden leak information surveillance carry us government mr snowden ex-us intelligence contractor leak document media highlight various method use agency gather information ask use computer scan enormous number email look pattern behavior suggest terrorist activity sir malcolm reply think be reasonable argument say human eye be read innocent people email do have extract vast number internet communication tiny number relate serious terrorist plot start recognize modern world terrorist use technology available be foolish intelligence agency free society start use technology be allow determine rule be do moment parliament have lay rule want look be question proportionality sir malcolm committee be carry inquiry uk intelligence agency access people private information ";
    	AlchemyAPI api = new AlchemyAPI(ConfigUtil.getValue("ALCHEMY_API"), ConfigUtil.getValue("ALCHEMY_API_KEY"));
    	System.out.println(api.GetRankedNamedEntities(text).asString());
    }
}
