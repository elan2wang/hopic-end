package elan.nlp.models;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import elan.nlp.util.FileUtil;
import elan.nlp.util.json.JsonFactory;

public class Results {

	protected int top_k = 10;
	protected int ntopics;
	protected int nslices;
	// used to generate output
	protected int[][] docsCount_perSlice_perTopic;
	protected String[][][] topWords_perSlice_perTopic;

	public Results(){}

	public void outputDocsCount_perSlice_perTopic(String pathname) {
		FileWriter fw = FileUtil.open(pathname);
		for (int i=0; i<docsCount_perSlice_perTopic.length; i++) {
			for (int j=0; j<docsCount_perSlice_perTopic[i].length; j++) {
				FileUtil.append(fw, docsCount_perSlice_perTopic[i][j]+"");
				if (j != docsCount_perSlice_perTopic[i].length-1) {
					FileUtil.append(fw, ", ");
				}
			}
			if (i != docsCount_perSlice_perTopic.length-1) {
				FileUtil.append(fw, "\n");
			}
		}
		FileUtil.close(fw);
	}
	
	@SuppressWarnings("rawtypes")
	public String toJson() throws IOException{
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("slice_num", nslices);
		map.put("topic_num", ntopics);

		// set documents count for each topic within each slice
		List<Object> docs_count = new ArrayList<Object>();
		for (int i=0; i<docsCount_perSlice_perTopic.length; i++){
			HashMap<String, Object> slice= new LinkedHashMap<String, Object>();

			List<Integer> counts = new ArrayList<Integer>();
			for (int j=0; j<docsCount_perSlice_perTopic[i].length; j++){
				counts.add(docsCount_perSlice_perTopic[i][j]);
			}
			slice.put("counts", counts);

			docs_count.add(slice);
		}
		map.put("slices_count", docs_count);

		// set top words for each topic within each slice 
		List<HashMap> topWords = new ArrayList<HashMap>();
		for (int i=0; i<topWords_perSlice_perTopic.length; i++) {
			HashMap<String, Object> slice = new LinkedHashMap<String, Object>();
			List<HashMap> list = new ArrayList<HashMap>();
			for (int j=0; j<ntopics; j++) {
				LinkedHashMap<String, Object> words = new LinkedHashMap<String, Object>();
				words.put("topwords", Arrays.asList(topWords_perSlice_perTopic[i][j]));
				list.add(words);
			}
			slice.put("topics", list);
			topWords.add(slice);
		}
		map.put("slices_topwords", topWords);
		System.out.print(JsonFactory.toJson(map));

		return JsonFactory.toJson(map);
	}

	public String[][] topWords(double[][] phi, HashMap<String, Integer> vocabulary, int k) {
		String[][] topWords = new String[phi.length][k];
		HashMap<Integer, String> vo = transfer(vocabulary);
		for (int i=0; i<phi.length; i++) {
			int[] nums = selectK(phi[i], k);
			topWords[i] = number2word(nums, vo);
		}

		return topWords;
	}

	public Integer[] decideTopics(double[][] theta) {
		Integer[] topics = new Integer[theta.length];
		for (int i=0; i<theta.length; i++) {
			double max = theta[i][0];
			int maxIndex = 0;
			for (int j=1; j<theta[i].length; j++) {
				if (theta[i][j] > max) {
					max = theta[i][j];
					maxIndex = j;
				}
			}
			topics[i] = maxIndex;
		}

		return topics;
	}
	
	class HwordComparable implements Comparator<Integer> {
		private double[] sortProb;

		public HwordComparable(double[] sortProb) {
			this.sortProb = sortProb.clone();
		}

		public int compare(Integer o1, Integer o2) {
			if(sortProb[o1] > sortProb[o2]) return -1;  
			else if(sortProb[o1] < sortProb[o2]) return 1;  
			else return 0;  
		}
	}

	private int[] selectK(double[] a, int k){
		int[] selected = new int[k];

		Integer[] termIndex = new Integer[a.length];
		for(int i=0; i<a.length; i++) termIndex[i] = new Integer(i);

		Arrays.sort(termIndex, new HwordComparable(a));

		for (int i=0; i<k; i++) {
			selected[i] = termIndex[i];
		}
		return selected;
	}

	private HashMap<Integer, String> transfer(HashMap<String, Integer> vocabulary) {
		HashMap<Integer, String> vo = new HashMap<Integer, String>();
		for (Entry<String, Integer> entry : vocabulary.entrySet()) {
			vo.put(entry.getValue(), entry.getKey());
		}

		return vo;
	}

	private String[] number2word(int[] numbers, HashMap<Integer, String> vocabulary) {
		String[] words = new String[numbers.length];

		for (int i=0; i<numbers.length; i++) {
			words[i] = vocabulary.get(numbers[i]);
		}

		return words;
	}

}
