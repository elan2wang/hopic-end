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
    
    public Response getRankedNamedEntities(String url) {
    	params.addParameter("url", url);
    	return client.get(_requestUri, params.toArray());
    }
    
    public static void main(String args[]) {
    	AlchemyAPI api = new AlchemyAPI(ConfigUtil.getValue("ALCHEMY_API"), ConfigUtil.getValue("ALCHEMY_API_KEY"));
    	System.out.println(api.getRankedNamedEntities("http://www.theguardian.com/world/2013/jun/10/edward-snowden-china-hong-kong").asString());
    }
}
