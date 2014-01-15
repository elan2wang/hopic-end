package elan.nlp.crawler.http;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClient implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);
	private static final long serialVersionUID		= 1L;

	private String token;
	private org.apache.http.client.HttpClient client = null;

	public HttpClient(){
		client = new DefaultHttpClient();
	}

	public Response get(String url){
		return get(url, new QueryParameter[0]);
	}

	public Response get(String url, QueryParameter[] params){
		if(params != null && params.length > 0){
			String encodedParams = HttpClient.encodeParameters(params);
			if(-1 == url.indexOf("?")){
				url += "?" + encodedParams;
			} else {
				url += "&" + encodedParams;
			}
		}
		logger.debug(url);
		HttpGet httpGet = new HttpGet(url);
		return httpRequest(httpGet);
	}

	public Response httpRequest(HttpRequestBase method){
		return httpRequest(method, false);
	}

	public Response httpRequest(HttpRequestBase method, Boolean WithTokenHeader){
		Response res = null;

		/* set request header */
		List<Header> headers = new ArrayList<Header>();
		if(WithTokenHeader){
			if (token == null){
				throw new IllegalStateException("OAuth2 token is not set");
			}
			headers.add(new BasicHeader("Authorization", "OAuth2 " + token));
		}
		String user_agent = "Mozilla/5.0 (Windows NT 6.2) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172 Safari/537.22";
		headers.add(new BasicHeader("user-agent",user_agent));
		for(Header header : headers){
			method.setHeader(header);
		}
		
		try {
			/* execute the request */
			HttpResponse response = client.execute(method);
			
			/* check response, if status code is not 200, throw HttpResponseException */
			StatusLine statusLine = response.getStatusLine();
			if(statusLine.getStatusCode() > 200){
				logger.error(statusLine.getStatusCode()+",http request failed: " + method.getURI().toString());
			}
			
			/* generate CrawlResponse */
			HttpEntity entity = response.getEntity();
			try {
				res = new Response(statusLine.getStatusCode(), EntityUtils.toByteArray(entity));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (ClientProtocolException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		return res;
	}

	public static String encodeParameters(QueryParameter[] queryParams){
		StringBuffer buf = new StringBuffer();
		for(QueryParameter queryParam : queryParams){
			try {
				buf.append(URLEncoder.encode(queryParam.getName(), "UTF-8"))
				.append("=")
				.append(URLEncoder.encode(queryParam.getValue(), "UTF-8"))
				.append("&");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		return buf.deleteCharAt(buf.length()-1).toString();
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
