package elan.nlp.models.dtm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import elan.nlp.util.FileUtil;

public class DtmModel {

	private int nslices;
	private int ntopics;
	private double alpha;
	
	private String corpus_prefix;
	private String outname;
	
	// theta[d][t]: proportion of document [d] with topic [t]
	double[][] theta;
	// beta_t[s][t][w]: proportion of word [w] with topic [t] in time slice [s]
	double[][][] beta_t;
	
	public DtmModel(String corpus_prefix, String outname, double alpha, int ntopics, int nslices) {
		this.corpus_prefix = corpus_prefix;
		this.outname = outname;
		this.alpha = alpha;
		this.ntopics = ntopics;
		this.nslices = nslices;
	}
	
	public int exec() {
		int exitValue = 0;
		try {
			Process process = Runtime.getRuntime().exec("/Users/wangjian/Workspace/dtm_release/dtm/main "
					+ "--ntopics=20 --mode=fit --rng_seed=0 --initialize_lda=true --corpus_prefix="+corpus_prefix+" "
					+ "--outname="+outname+" --top_chain_var=0.005 --alpha="+alpha+" "
					+ "--lda_sequence_min_iter=6 --lda_sequence_max_iter=20 --lda_max_em_iter=10");
			
			BufferedReader read = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			String line = null;
			while( (line = read.readLine()) != null) {
				System.out.println(line);
			}
			
			exitValue = process.waitFor();
			read.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return exitValue;
	}
	
	public void readTheta() {
		String theta_filename = outname+"/lda-seq/gam.dat";
		List<Double> thetaList = new ArrayList<Double>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(theta_filename)));
			String line = null;
			while((line = br.readLine()) != null) {
				thetaList.add(Double.valueOf(line));
			}
			br.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		
		theta = new double[thetaList.size()/ntopics][ntopics];
		double rowsum = 0.00;
		for (int i=0;i<thetaList.size();i++) {
			rowsum += thetaList.get(i);
			theta[i/ntopics][i%ntopics] = thetaList.get(i);
			
			if ((i+1)%ntopics == 0) {
				for(int j=0; j<ntopics; j++) {
					theta[i/ntopics][j] = theta[i/ntopics][j]/rowsum;
				}
				rowsum = 0.00;
			}
		}
	}
	
	public void readBetas() {
		String betafile_dir = outname+"/lda-seq";
		File files[] = new File(betafile_dir).listFiles();
		beta_t = new double[nslices][ntopics][];
		for (File docFile : files) {
			String filename = docFile.getName();
			if (!filename.endsWith("-var-e-log-prob.dat")) continue;
			Integer t = Integer.valueOf(filename.split("-")[1]);
			// go through the file
			List<Double> betaList = new ArrayList<Double>();
			try {
				BufferedReader br = new BufferedReader(new FileReader(docFile));
				String line = null;
				while((line = br.readLine()) != null) {
					betaList.add(Double.valueOf(line));
				}
				br.close();
			} catch (NumberFormatException | IOException e) {
				e.printStackTrace();
			}
			
			// assign to beta_t
			for (int i=0; i<nslices; i++) beta_t[i][t] = new double[betaList.size()/nslices];
			for (int i=0; i<betaList.size(); i++) {
				beta_t[i%nslices][t][i/nslices] = Math.exp(betaList.get(i));
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		DtmModel dtm = new DtmModel("News/guardian/snowden/dtm", "News/guardian/snowden/dtm/topics_15/even", 0.01, 15, 12);
		
		dtm.readTheta();
		dtm.readBetas();
		
		DtmResults res = new DtmResults(DtmDocuments.readTimeSlices("News/guardian/snowden/dtm/snowden-seq-kmeans.dat"),
				dtm.theta, dtm.beta_t, "News/guardian/snowden/dtm/snowden-dict.dat");
		
		FileWriter fw = FileUtil.open("News/guardian/snowden/dtm/topics_15/even/res.json");
		FileUtil.append(fw, res.toJson());
		FileUtil.close(fw);
		
		res.outputDocsCount_perSlice_perTopic("News/guardian/snowden/dtm/topics_15/even/docs_distribution_kmeans.dat");
		
	}	
}
