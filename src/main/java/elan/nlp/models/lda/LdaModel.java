package elan.nlp.models.lda;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import elan.nlp.util.FileUtil;

public class LdaModel {

	private int[][] documents;
	private HashMap<String, Integer> dicts;

	private LdaResults results;
	
	private Integer nslices;
	private Integer ntopics;
	private Integer top_k;

	public LdaModel(int nslices, int ntopics, int top_k) {
		this.nslices = nslices;
		this.ntopics = ntopics;
		this.top_k = top_k;
		
		dicts = LdaDocuments.readDict("News/guardian/snowden/lda/snowden-dict.dat");
		results = new LdaResults(this.nslices, this.ntopics, this.top_k);
	}

	public void run(String seqFilePath, Integer t) {
		// documents
		documents = LdaDocuments.readDocs("News/guardian/snowden/lda/snowden-docs.dat", seqFilePath, t);

		// vocabulary size
		int V = dicts.size();

		// # topics
		int K = ntopics;

		// good values alpha = 2, beta = .5
		double alpha = 2;
		double beta = .5;

		System.out.println("Latent Dirichlet Allocation using Gibbs Sampling.");

		LdaGibbsSampler gibbs = new LdaGibbsSampler(documents, V);
		gibbs.configure(1000, 200, 10, 3);
		gibbs.gibbs(K, alpha, beta);

		double[][] theta = gibbs.getTheta();
		double[][] phi = gibbs.getPhi();

		// generate result
		results.generate(t, theta, phi, dicts);
		results.output(theta, "News/guardian/snowden/lda/topics_15/kmeans/theta_"+t+".dat");
		results.output(phi, "News/guardian/snowden/lda/topics_15/kmeans/phi_"+t+".dat");
		
	}

	public static void main(String[] args) throws IOException {
		LdaModel lda = new LdaModel(12, 15, 10);

		for (int t=1; t<=lda.nslices; t++) {
			System.out.println("\n%%%%%%%%%%%%%%%% run lda at slice "+t+" %%%%%%%%%%%%%%%%");
			
			lda.run("News/guardian/snowden/lda/snowden-seq-kmeans.dat", t);
		}
		
		FileWriter fw = FileUtil.open("News/guardian/snowden/lda/topics_15/kmeans/res.json");
		FileUtil.append(fw, lda.results.toJson());
		FileUtil.close(fw);
		
		lda.results.outputDocsCount_perSlice_perTopic("News/guardian/snowden/lda/topics_15/kmeans/docs_distribution.dat");
		
	}
}
