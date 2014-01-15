package elan.nlp.models.dtm;

import elan.nlp.models.Documents;
import elan.nlp.models.Results;

public class DtmResults extends Results {

	private int[] slices;
	// theta[d][t]: proportion of document [d] with topic [t]
	private double[][] theta;
	// beta_t[s][t][w]: proportion of word [w] with topic [t] in time slice [s]
	private double[][][] beta_t;

	public DtmResults(int[] slices, double[][] theta, double[][][] beta_t) {
		super();
		this.theta = theta;
		this.beta_t = beta_t;
		this.slices = slices;

		ntopics = theta[0].length;
		nslices = slices.length;
		docsCount_perSlice_perTopic = new int[nslices][ntopics];
		topWords_perSlice_perTopic = new String[nslices][ntopics][top_k];

		init();
	}

	private void init() {
		
		for (int i=0; i<this.nslices; i++) {
			topWords_perSlice_perTopic[i] = topWords(beta_t[i], Documents.readDict("News/dtm/bbc-dict.dat"), top_k);
		}

		int s = 0;
		for (int i=0; i<nslices; i++) {
			double[][] tmp = new double[slices[i]][ntopics];
			for (int j=0; j<slices[i]; j++) {
				for (int k=0; k<theta[s+j].length; k++) {
					tmp[j][k] = theta[s+j][k];
				}
			}

			Integer[] topics = decideTopics(tmp);
			for (int j=0; j<topics.length; j++) {
				docsCount_perSlice_perTopic[i][topics[j]]++;
			}

			s += slices[i];
		}
	}
}
