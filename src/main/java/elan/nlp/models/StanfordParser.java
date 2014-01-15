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
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import elan.nlp.util.ConfigUtil;

public class StanfordParser {
	private static StanfordParser instance = null;

	private StanfordCoreNLP pipeline;
	private List<String> tagFilter;
	
	private StanfordParser() {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization and NER
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
		
		// read tag filter from configuration
		String[] tags = ConfigUtil.getValue("TAG_FILTER").split(",");
		tagFilter = Arrays.asList(tags);
	}

	public static StanfordParser getInstance() {
		if (instance == null) {
			instance = new StanfordParser();
		}
		return instance;
	}

	public void process(String inFilepath, String outFilepath) {
		
		try {
			StringBuilder inText = new StringBuilder();
			StringBuilder outText = new StringBuilder();
			
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
					String pos = token.get(PartOfSpeechAnnotation.class);
					if (tagFilter.contains(pos)) {
						String lemma = token.get(LemmaAnnotation.class);
						outText.append(lemma.toLowerCase()+" ");
					}
				}
			}
			
			// write the processed text to output file
			File file = new File(outFilepath);
			if (file.exists()) file.delete();
			FileWriter fw = new FileWriter(outFilepath);
			fw.append(outText);
			fw.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public void batchProcess(String srcDir, String dstDir) {
		int i = 0;
		File dir = new File(srcDir);
		File[] files = dir.listFiles();
		for (File file : files) {
			String filename = file.getName();
			if (filename.startsWith(".") || !filename.endsWith(".txt")) continue;
			process(srcDir+"/"+filename, dstDir+"/"+filename);
			// debug
			System.out.print(".");
			if (i++%20 == 0) System.out.print("\n");
		}

	}
	
	public static void main(String[] args){
		StanfordParser parser = StanfordParser.getInstance();
		parser.batchProcess("News/fulltext", "News/fulltext/all");
	}

}
