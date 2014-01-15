package elan.nlp.models.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import elan.nlp.models.Documents;
import elan.nlp.util.FileUtil;

public class LdaDocuments extends Documents {

	private Integer dictIndex = 0;
	private List<Document> documents;

	public LdaDocuments(String docsPath, String stopWordsFilepath) {
		super(stopWordsFilepath);
		documents = new ArrayList<Document>();
		
		// call init()
		init(docsPath);
		// exclude infrequent words
		excludeInferquentWords();
	}

	private void init(String docsPath) {
		File files[] = new File(docsPath).listFiles();
		for (File docFile : files) {
			if (!docFile.getName().endsWith(".txt")) continue;
			Document doc = new Document(docFile.getAbsolutePath());
			documents.add(doc);
		}
		System.out.println("Total words count: "+vocabulary_count.size()+"\nTotal useful words count: "+dictionary.size());
	}

	private void excludeInferquentWords() {
		for (int i=0; i<documents.size(); i++) {
			Document doc = documents.get(i);
			List<String> delList = new ArrayList<String>();
			for (String word : doc.words) {
				if (!dictionary.containsKey(word)) delList.add(word);
			}
			doc.words.removeAll(delList);
		}
	}

	public void exportDocuments(String filename) {
		FileWriter fw = FileUtil.open(filename);
		for (int i=0;i<documents.size(); i++) {
			StringBuilder sb = new StringBuilder();
			List<String> words = documents.get(i).words;
			for(int j=0;j<words.size();j++) {
				sb.append(dictionary.get(words.get(j))+" ");
			}
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
			}
			FileUtil.append(fw, sb.toString());
		}
		FileUtil.close(fw);
	}

	public int[][] asArrays() {
		int[][] docs = new int[documents.size()][];
		for (int i=0;i<documents.size(); i++) {
			List<String> words = documents.get(i).words;
			docs[i] = new int[words.size()];
			for(int j=0;j<words.size();j++) {
				docs[i][j] = dictionary.get(words.get(j));
			}
		}
		return docs;
	}
	
	class Document{
		List<String> words = new ArrayList<String>();

		public Document(String pathname) {
			init(pathname);
		}

		private void init(String pathname) {
			try {
				// open file reader
				BufferedReader reader = new BufferedReader(new FileReader(pathname));
				// read by line
				String line = null;
				while((line = reader.readLine()) != null) {
					// tokenization
					StringTokenizer tokenizer = new StringTokenizer(line);
					while(tokenizer.hasMoreElements()) {
						String token = tokenizer.nextToken();
						if (!stopwords.contains(token)  && token.length() > 1) {
							// index and count vocabulary
							if (!vocabulary_count.containsKey(token)) {
								vocabulary_count.put(token, 1);
							} else {
								vocabulary_count.put(token, vocabulary_count.get(token)+1);
							}
							// if the frequency is already larger than BASE_FREQUENCY,
							// add it to dictionary
							if (vocabulary_count.get(token) > BASE_FREQUENCY && !dictionary.containsKey(token)){
								dictionary.put(token, dictIndex++);
							}
							words.add(token);
						}
					}	
				}
				// close file reader
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static int[][] readDocs(String filename) {
		int[][] docs = null;
		
		return docs;
	}
	
	public static void main(String[] args) {

		LdaDocuments docs = new LdaDocuments("News/fulltext/all", "stopwords.txt");
		docs.exportDocuments("News/lda_docs.txt");
		docs.exportDictionary("News/lda_dicts.txt");

	}
}
