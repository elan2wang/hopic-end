package elan.nlp.crawler;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import elan.nlp.crawler.http.Response;
import elan.nlp.crawler.readability.WebContentExtractor;
import elan.nlp.util.FileUtil;

public class Extractor extends BaseCrawler {
	
	private FileWriter error_file;
	
	public Extractor(String url, String path, FileWriter error_file) {
		super(url, path);
		this.error_file = error_file;
	}

	public void run() {
		System.out.print("+");
		File file = new File(path);
		if (file.exists()) return;
		
		Response response = client.get(url);
		if (response == null) {
			FileUtil.append(error_file, path+"\t"+url+"\n");
			return;
		}
        Document doc = Jsoup.parse(response.asString());
        Charset charset = WebContentExtractor.getCharset(doc);
        WebContentExtractor.cleanDocument(doc);
        String content = new String(WebContentExtractor.grapArticle(doc).getBytes(), charset);
        
        FileWriter fw = FileUtil.open(file);
        FileUtil.append(fw, content);
        FileUtil.close(fw);
        System.out.print(".");
	}
}
