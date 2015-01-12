package elan.nlp.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import elan.nlp.util.ConfigUtil;
import elan.nlp.util.FileUtil;

public class StanfordParser {
	private static StanfordParser instance = null;

	private Integer totalWords = 0;
	private Integer remainWords = 0;
	
	private StanfordCoreNLP pipeline;
	private List<String> tagFilter;
	private List<String> nerFilter;
	
	private StanfordParser() {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization and NER
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
		pipeline = new StanfordCoreNLP(props);
		
		// read tag filter from configuration
		String[] tags = ConfigUtil.getValue("TAG_FILTER").split(",");
		tagFilter = Arrays.asList(tags);
		// read ner filter from configuration
		String[] ners = ConfigUtil.getValue("NER_FILTER").split(",");
		nerFilter = Arrays.asList(ners);
	}

	public static StanfordParser getInstance() {
		if (instance == null) {
			instance = new StanfordParser();
		}
		return instance;
	}

	public void process(String inFilepath, String outFilepath, String nerOutFile) {
		
		try {
			StringBuilder inText = new StringBuilder();
			StringBuilder outText = new StringBuilder();
			StringBuilder nerText = new StringBuilder();
			
			// read some text in the inText variable from input file
			BufferedReader reader = new BufferedReader(new FileReader(inFilepath));
			String line = null;
			while((line = reader.readLine()) != null) {
				if (line.trim().length() == 0) continue;
				inText.append(line+"\n");
			}
			reader.close();
			
			// create an empty Annotation just with the given text
			Annotation document = new Annotation(inText.toString());

			// run all Annotators on this text
			pipeline.annotate(document);

			// these are all the sentences in this document
			// a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);

			for(CoreMap sentence: sentences) {
				// traversing the words in the current sentence
				// a CoreLabel is a CoreMap with additional token-specific methods
				for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
					totalWords++;
					String pos = token.tag();
					if (tagFilter.contains(pos)) {
						remainWords++;
						String lemma = token.lemma();
						outText.append(lemma+" ");
						if (nerFilter.contains(token.ner())) {
							nerText.append(token.word()+" ");
						}
					}
				}
			}
			
			// write the processed text to output file
			FileWriter fw = FileUtil.open(outFilepath);
			fw.append(outText);
			FileUtil.close(fw);
			
			if (nerOutFile != null) {
				FileWriter fw2 = FileUtil.open(nerOutFile);
				fw2.append(nerText);
				FileUtil.close(fw2);
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void batchProcess(String srcDir, String dstDir, String nerDir) {
		int i = 0;
		File dir = new File(srcDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			String filename = file.getName();
			if (filename.startsWith(".") || !filename.endsWith(".txt")) continue;
			process(srcDir+"/"+filename, dstDir+"/"+filename, nerDir+"/"+filename);
			// debug
			System.out.print(".");
			if (i++%20 == 0) System.out.print("\n");
		}

	}
	
	public static void main(String[] args){
		StanfordParser parser = StanfordParser.getInstance();
		String baseDir = "News/guardian/snowden";
		parser.batchProcess(baseDir+"/fulltext", baseDir+"/fulltext/parse", baseDir+"/fulltext/ners");
		
		System.out.println("total words: "+parser.totalWords+"\nremaining words: "+parser.remainWords);
	}

}
