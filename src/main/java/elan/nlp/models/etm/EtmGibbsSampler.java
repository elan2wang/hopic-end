package elan.nlp.models.etm;

/*
 * Paper reference: Hyungsul Kim, Yizhou Sun, Julia Hockenmaier, and Jiawei Han, "ETM: Entity Topic Models for Mining Documents Associated with Entities", Proc. of 2012 IEEE Int. Conf. on Data Mining (ICDM'12), Brussels, Belgium, Dec. 2012
 */


public class EtmGibbsSampler {

	/* gibbs sampler parameters */
	private static final int BURN_IN = 100;			// burn-in period
	private static final int INTERATIONS = 1000;	// max iterations
	private static final int SAMPLE_INTERVAL = 20;	// sample interval

	/* corpus statistic */
	int[][] documents;	// word list in each document
	int[][] entities;	// entity list in each document
	int V, K, E;		// number of vocabulary, topics, entities

	/* Dirichlet hyper-parameters */
	double alpha_0;		// document -- topic
	double alpha_1;		// entity   -- topic
	double beta_0;		// word     -- topic
	double gamma_0;		// entity   -- topic

	/* linear combination coefficient */
	double beta_1;
	double gamma_1;

	/* multiple distribution data */
	double[][] phi_sum;		// cumulative statistics of phi
	double[][] varphi_sum;	// cumulative statistics of varphi
	double[][][] psi_sum;	// cumulative statistics of psi
	double[][] theta_sum;
	double[][] vartheta_sum;
	int numstats;
	
	/* statistic data */
	int ze[][][];		// topic and entity assignment for each word
	
	int nw[][][];	// nw[V][K][E]	count of tokens of word [w] assigned to topic[k] and entity[e]
	int nwsum[][];	// nw_sum[K][E]	total count of word tokens assigned to topic[k] and entity[e]
	int nd[][][];	// nd[M][K][E]	
	int ndsum[];	// ndsum[M]
	
	int z_nw[][];	// z_nw[V][K]
	int z_nwsum[];	// z_nwsum[K]
	int z_nd[][];	// z_nd[M][K]
	int z_ndsum[];	// z_ndsum[M]
	
	int e_nw[][];	// e_nw[V][K]
	int e_nwsum[];	// e_nwsum[K]
	int e_nd[][];	// e_nd[M][K]
	int e_ndsum[];	// e_ndsum[M]

	public EtmGibbsSampler(int[][] documents, int entities[][], 
			int V, int E, double beta_1, double gamma_1, double beta_0,
			double gamma_0, double alpha_0, double alpha_1) {
		this.documents = documents;
		this.V = V;
		this.E = E;
		
		this.beta_0 = beta_0;
		this.beta_1 = beta_1;
		this.gamma_0 = gamma_0;
		this.gamma_1 = gamma_1;
		this.alpha_0 = alpha_0;
		this.alpha_1 = alpha_1;
	}

	private void initialize(int K) {
		int M = documents.length;
		
		nw = new int[V][K][E];
		nwsum = new int[K][E];
		nd = new int[M][K][E];
		ndsum = new int[M];
		
		z_nw = new int[V][K];
		z_nwsum = new int[K];
		z_nd = new int[M][K];
		
		e_nw = new int[V][E];
		e_nwsum = new int[E];
		e_nd = new int[M][E]; 
		
		ze = new int[M][][];
		for (int m=0; m<M; m++) {
			int N = documents[m].length;
			ze[m] = new int[N][2];
			for (int n=0; n<N; n++) {
				// random sample topic
				int topic = (int)(Math.random() * K);
				// random sample entity
				// ???? whether it is essential to sample from entities[m]
				int entity = (int)(Math.random() * E);
				
				ze[m][n][0] = topic;
				ze[m][n][1] = entity;
				
				nw[documents[m][n]][topic][entity]++;
				nwsum[topic][entity]++;
				nd[m][topic][entity]++;
				
				z_nw[documents[m][n]][topic]++;
				z_nwsum[topic]++;
				z_nd[m][topic]++;
				
				e_nw[documents[m][n]][entity]++;
				e_nwsum[entity]++;
				e_nd[m][entity]++;
			}
			ndsum[m] = N;
		}
	}

	public void gibbs(int K) {
		this.K = K;
		
		theta_sum = new double[documents.length][K];
		vartheta_sum = new double[documents.length][E];
		phi_sum = new double[V][K];
		varphi_sum = new double[V][E];
		psi_sum = new double[K][E][V];
		
		initialize(K);
		
		System.out.println("Sampling "+ INTERATIONS + " interations with burn-in of "
				+ BURN_IN + " (B/S=" + SAMPLE_INTERVAL+ ").");
		
		int displayColumn = 0;
		for (int i = 0; i < INTERATIONS; i++) {
			for (int m = 0; m < ze.length; m++) {
				for (int n = 0; n < ze[m].length; n++) {
					int[] te = sampleFullConditional(m, n);
					ze[m][n] = te;
				}
			}
			
			if ((i < BURN_IN) && (i % 20 == 0)) {
				System.out.print("B");
				displayColumn++;
			}
			
			if ((i > BURN_IN) && (i % 20 == 0)) {
				System.out.print("S");
				displayColumn++;
			}
			
			if ((i > BURN_IN) && (i % SAMPLE_INTERVAL == 0)) {
				updateParams();
				System.out.print("|");
				if (i % 20 != 0) displayColumn++;
			}
			
			if (displayColumn >= 100) {
				System.out.println();
				displayColumn=0;
			}
		}
	}

	/**
	 * Sample a topic-entity pair <z_i, e_i> from full conditional distribution.
	 * 
	 * details can be found in the reference paper
	 */
	private int[] sampleFullConditional(int m, int n) {
		int[] te = ze[m][n];
		int topic = te[0];
		int entity = te[1];
		int t = documents[m][n];
		
		nw[t][topic][entity]--;
		nwsum[topic][entity]--;
		nd[m][topic][entity]--;
		
		z_nw[t][topic]--;
		z_nwsum[topic]--;
		z_nd[m][topic]--;
		
		e_nw[t][entity]--;
		e_nwsum[entity]--;
		e_nd[m][entity]--;
		
		
		// calculate the probability of each assignment
		double[][] p = new double[K][E];
		for (int k = 0; k < K; k++) {
			for (int e = 0; e < E; e++) {
				p[k][e] = 1;
				p[k][e] *= (z_nd[m][k] + alpha_0/K) / (ndsum[m] + alpha_0 -1);
				p[k][e] *= (e_nd[m][e] + alpha_1/entities[m].length) / (ndsum[m] + alpha_1 -1);
				p[k][e] *= (nw[t][k][e] + beta_1*((z_nw[t][k] + beta_0/V)/(z_nwsum[k] + beta_0))
							+ gamma_1*((e_nw[t][e] + gamma_0/V)/(e_nwsum[e] + gamma_0)))
							/ (nwsum[k][e] + beta_1 + gamma_1);
			}
		}
		
		// calculate the cumulate multinomial parameters
		for (int k = 0; k < K; k++) {
			if (k != 0) p[k][0] += p[k-1][E-1];
			for (int e = 1; e < E; e++) {
				p[k][e] += p[k][e-1];
			}
		}
		
		// scaled sample because of unnormalised p[][]
		double u = Math.random() * p[K-1][E-1];
		for (topic = 0; topic < K; topic++) {
			for (entity = 0; entity < E; entity++) {
				if (u < p[topic][entity]) break;
			}
		}
		
		nw[t][topic][entity]++;
		nwsum[topic][entity]++;
		nd[m][topic][entity]++;
		z_nw[t][topic]++;
		z_nwsum[topic]++;
		z_nd[m][topic]++;
		e_nw[t][entity]++;
		e_nwsum[entity]++;
		e_nd[m][entity]++;
		
		te[0] = topic;
		te[1] = entity;
		return te;
	}

	private void updateParams() {
		numstats++;
		
		// calculate phi_sum
		for (int k = 0; k < K; k++) {
			for (int t = 0; t < V; t++) {
				phi_sum[k][t] += (z_nw[t][k] + beta_0/V)/(z_nwsum[k] + beta_0);
			}
		}
		
		// calculate varphi_sum
		for (int e = 0; e < E; e++) {
			for (int t = 0; t < V; e++) {
				varphi_sum[e][t] += (e_nw[t][e] + gamma_0/V)/(e_nwsum[e] + gamma_0);
			}
		}

		// calculate psi_sum
		for (int k = 0; k < K; k++) {
			for (int e = 0; e < E; e++) {
				double tmp2 = 0.0;
				for (int t = 0; t < V; t++)
					tmp2 += beta_1 * phi_sum[k][t] + gamma_1 * varphi_sum[e][t];
				for (int t = 0; t < V; t++) {
					double tmp1 = beta_1 * phi_sum[k][t] + gamma_1 * varphi_sum[e][t];
					psi_sum[k][e][t] += (nw[t][k][e] + tmp1/numstats)/(nwsum[k][e] + tmp2/numstats);
				}
			}
		}
		
		// calculate theta & vartheta_sum
		for (int m = 0; m < documents.length; m++) {
			// theta
			for (int k = 0; k < K; k++) {
				int tmp = 0;
				for (int e = 0; e < E; e++) tmp += nd[m][k][e];
				theta_sum[m][k] += (tmp + alpha_0/K)/(ndsum[m] + alpha_0);
			}
			
			// var_theta
			for (int e = 0; e < E; e++) {
				int tmp = 0;
				for (int k = 0; k < K; k++) tmp += nd[m][k][e];
				vartheta_sum[m][e] += (tmp + alpha_1/E)/(ndsum[m] + alpha_1);
			}
		}
	}

	public double[][][] getPsi() {
		double[][][] psi = new double[K][E][V];
		for (int k=0; k<K; k++) {
			for (int e=0; e<E; e++) {
				for (int t=0; t<V; t++) {
					psi[k][e][t] = psi_sum[k][e][t]/numstats;
				}
			}
		}
		
		return psi;
	}
	
	public double[][] getTheta() {
		double[][] theta = new double[documents.length][K];
		for (int m=0; m<theta.length; m++) {
			for (int k=0; k<K; k++) {
				theta[m][k] = theta_sum[m][k]/numstats;
			}
		}
		
		return theta;
	}
	
	public double[][] getVarTheta() {
		double[][] vartheta = new double[documents.length][K];
		for (int m=0; m<vartheta.length; m++) {
			for (int k=0; k<K; k++) {
				vartheta[m][k] = vartheta_sum[m][k]/numstats;
			}
		}
		
		return vartheta;
	}
}
