package elan.nlp.models.dtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import elan.nlp.models.Documents;
import elan.nlp.util.FileUtil;

public class DtmDocuments extends Documents{

	private Integer dictIndex = 0;
	private List<Document> documents;

	public DtmDocuments(String docsPath, String stopWordsFilepath) {
		super(stopWordsFilepath);
		documents = new ArrayList<Document>();
		
		init(docsPath);
		excludeInfrequentWords();
	}

	private void init(String docsPath) {
		// files must be ordered by timestamp
		File files[] = new File(docsPath).listFiles();
		for (File docFile : files) {
			if (!docFile.getName().endsWith(".txt")) continue;
			Document doc = new Document(docFile.getAbsolutePath());
			documents.add(doc);
		}
		System.out.println("Total words count: "+vocabulary_count.size()+"\nTotal useful words count: "+dictionary.size());
	}

	private void excludeInfrequentWords() {
		for (int i=0; i<documents.size(); i++) {
			Document doc = documents.get(i);
			Iterator<Entry<String, Integer>> iter = doc.words.entrySet().iterator();
			List<Entry<String, Integer>> delList = new ArrayList<Entry<String, Integer>>();
			while(iter.hasNext()) {
				Entry<String, Integer> word = iter.next();
				if (!dictionary.containsKey(word.getKey())) delList.add(word);
			}
			doc.words.entrySet().removeAll(delList);
		}
		
	}
	
	public void exportDocuments(String filename) {
		FileWriter fw = FileUtil.open(filename);
		for (int i=0;i<documents.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(documents.get(i).words.size()+" ");
			Iterator<Entry<String, Integer>> iter = documents.get(i).words.entrySet().iterator();
			while(iter.hasNext()) {
				Entry<String, Integer> word = iter.next();
				sb.append(dictionary.get(word.getKey())+":"+word.getValue()+" ");
			}
			if (sb.length() > 0){
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
			}
			
			FileUtil.append(fw, sb.toString());
		}
		FileUtil.close(fw);
	}
	
	public static int[] readTimeSlices(String filename) {
		int [] slices = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(filename)));
			String line = br.readLine();
			if (line != null) {
				slices = new int[Integer.valueOf(line)];
				int i=0;
				while((line = br.readLine()) != null) {
					slices[i++] = Integer.valueOf(line);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return slices;
	}
	
	class Document {
		// vocabularies of this doc(with stop words excluded) and their frequency
		// key-index(Integer), value-frequency(Integer)
		HashMap<String, Integer> words;

		public Document(String filename) {
			words = new HashMap<String, Integer>();

			init(filename);
		}
		// initialize this doc
		private void init(String filename) {
			try {
				// open file reader
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				// read by line
				String line = null;
				while((line = reader.readLine()) != null) {
					// tokenization
					StringTokenizer tokenizer = new StringTokenizer(line);
					while(tokenizer.hasMoreElements()) {
						String token = tokenizer.nextToken();
						//System.out.println(token);
						if (!stopwords.contains(token) && token.length() > 1) {
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
							
							// update current document info
							if (words.containsKey(token)) {
								words.put(token, words.get(token)+1);
							} else {
								words.put(token, 1);
							}
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

	public static void main(String[] args) {
		DtmDocuments docs = new DtmDocuments("News/fulltext/all", "stopwords.txt");
		docs.exportDictionary("News/dtm_dict.dat");
		docs.exportDocuments("News/dtm_docs.dat");
	}
}
