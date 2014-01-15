package elan.nlp.models.lda;

import elan.nlp.models.Results;

public class LdaModel {

	public static void main(String[] args) {
		
		LdaDocuments docs = new LdaDocuments("News/fulltext/US Government Shutdown", "stopwords.txt");
		docs.exportDocuments("News/lda/lda_docs.txt");
		docs.exportDictionary("News/lda/lda_dicts.txt");
		docs.exportVocabularyFrequency("News/lda/lda_counts.txt");

		int[][] documents = docs.asArrays();

		// vocabulary
		int V = docs.getDictionary().size();
		System.out.println(V+"");
		// # topics
		int K = 10;
		// good values alpha = 2, beta = .5
		double alpha = 2;
		double beta = .5;

		System.out.println("Latent Dirichlet Allocation using Gibbs Sampling.");

		LdaGibbsSampler lda = new LdaGibbsSampler(documents, V);
		lda.configure(1000, 200, 10, 3);
		lda.gibbs(K, alpha, beta);

		double[][] theta = lda.getTheta();
		double[][] phi = lda.getPhi();

		System.out.println();
		System.out.println();
		System.out.println("Document--Topic Associations, Theta[d][k] (alpha="
				+ alpha + ")");
		System.out.print("d\\k\t");
		for (int m = 0; m < theta[0].length; m++) {
			System.out.print("   " + m % 10 + "    ");
		}
		System.out.println();
		for (int m = 0; m < theta.length; m++) {
			System.out.print(m + "\t");
			for (int k = 0; k < theta[m].length; k++) {
				// System.out.print(theta[m][k] + " ");
				System.out.print(LdaGibbsSampler.shadeDouble(theta[m][k], 1) + " ");
			}
			System.out.println();
		}
		System.out.println();

		Results res = new Results();
		res.topWords(phi, docs.getDictionary(), 15);
		System.out.println();

	}
}
