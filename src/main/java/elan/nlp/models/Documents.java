package elan.nlp.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import elan.nlp.util.FileUtil;
import elan.nlp.util.WordsLoader;

public abstract class Documents {

	// words with frequency less than this value will be excluded
	protected static int BASE_FREQUENCY = 2;

	// vocabularies of the corpus with stop words and infrequent words excluded
	// key-word(String), value-index(Integer)
	protected HashMap<String, Integer> dictionary;

	// vocabularies of the corpus(with stop words excluded) and their frequencies
	// key-word(String), value-frequency(Integer)
	protected HashMap<String, Integer> vocabulary_count;

	// stop words need to be excluded
	protected Set<String> stopwords;


	public Documents(String stopWordsFilepath) {
		// build stop words set
		stopwords = WordsLoader.getWordSet(new File(stopWordsFilepath));

		// initialize other fields
		dictionary = new HashMap<String, Integer>();
		vocabulary_count = new HashMap<String, Integer>();
	}

	public abstract void exportDocuments(String filename);

	public void exportDictionary(String filename) {
		FileWriter fw = FileUtil.open(filename);
		for (Entry<String, Integer> word : dictionary.entrySet()) {
			FileUtil.append(fw, word.getKey()+"\t"+word.getValue()+"\n");
		}
		FileUtil.close(fw);
	}

	public void exportVocabularyFrequency(String filename) {
		FileWriter fw = FileUtil.open(filename);
		for (Entry<String, Integer> word : vocabulary_count.entrySet()) {
			FileUtil.append(fw, word.getKey()+"\t"+word.getValue()+"\n");
		}
		FileUtil.close(fw);
	}

	public static HashMap<String, Integer> readDict(String filename) {
		HashMap<String, Integer> dicts = new HashMap<String, Integer>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));
			String line = null;
			while((line = reader.readLine()) != null) {
				String[] entry = line.split("\t");
				dicts.put(entry[0], Integer.valueOf(entry[1]));
			}
			reader.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return dicts;
	}
	
	//~~ getters ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public HashMap<String, Integer> getDictionary() {
		return dictionary;
	}

	public HashMap<String, Integer> getVocabulary_count() {
		return vocabulary_count;
	}

	public Set<String> getStopwords() {
		return stopwords;
	}
}
