package elan.nlp.models.lda;

import java.io.FileWriter;
import java.util.HashMap;

import elan.nlp.models.Results;
import elan.nlp.util.FileUtil;

public class LdaResults extends Results {

	public LdaResults(int nslices, int ntopics, int top_k) {
		this.nslices = nslices;
		this.ntopics = ntopics;
		this.top_k = top_k;
		
		docsCount_perSlice_perTopic = new int[nslices][ntopics];
		topWords_perSlice_perTopic = new String[nslices][ntopics][top_k];
	}

	public void generate(int t, double[][] theta, double[][] phi, HashMap<String, Integer> dicts) {
		topWords_perSlice_perTopic[t-1] = topWords(phi, dicts, top_k);
		
		Integer[] topics = decideTopics(theta);
		for (int j=0; j<topics.length; j++) {
			docsCount_perSlice_perTopic[t-1][topics[j]]++;
		}
		
	}
	
	public void output(double[][] data, String filepath) {
		FileWriter fw = FileUtil.open(filepath);
		
		for (int i=0; i< data.length; i++) {
			StringBuilder sb = new StringBuilder();
			for (int j=0; j<data[i].length; j++) {
				sb.append(data[i][j]+"\n");
			}
			FileUtil.append(fw, sb.toString());
		}
		
		FileUtil.close(fw);
	}
}
