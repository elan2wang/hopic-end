package elan.nlp.crawler;

import java.io.FileWriter;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import elan.nlp.crawler.http.Response;
import elan.nlp.crawler.readability.WebContentExtractor;

public class Extractor extends BaseCrawler {
	
	public Extractor(String url, String path) {
		super(url, path);
	}

	public void run() {
		System.out.print("+");
		Response response = client.get(url);
        Document doc = Jsoup.parse(response.asString());
        Charset charset = WebContentExtractor.getCharset(doc);
        WebContentExtractor.cleanDocument(doc);
        String content = new String(WebContentExtractor.grapArticle(doc).getBytes(), charset);
        
        FileWriter fw = openOutputFile(path);
        itemWrite(fw, content);
        closeOutputFile(fw);
        System.out.print(".");
	}
}
