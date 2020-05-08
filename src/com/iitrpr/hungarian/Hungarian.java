package com.iitrpr.hungarian;

import java.util.Arrays;



public class Hungarian {
	private final double[][] costMatrix;
	// private final ArrayList<ArrayList<Double>> costMatrix;
	private final int rows, cols, dim;
	private final double[] labelByWorker, labelByJob;
	private final int[] minSlackWorkerByJob;
	private final double[] minSlackValueByJob;
	private final int[] matchJobByWorker, matchWorkerByJob;
	private final int[] parentWorkerByCommittedJob;
	private final boolean[] committedWorkers;

	public Hungarian(double[][] costMatrix)
	// public Hungarian(ArrayList<ArrayList<Double>> costMatrix)
	{
		this.dim = Math.max(costMatrix.length, costMatrix[0].length);
		// this.dim=Math.max(costMatrix.size(), costMatrix.get(0).size());
		this.rows = costMatrix.length;
		// this.rows = costMatrix.size();
		this.cols = costMatrix[0].length;
		// this.cols=costMatrix.get(0).size();
		this.costMatrix = new double[this.dim][this.dim];
		// this.costMatrix=new ArrayList();
		for (int w = 0; w < this.dim; w++) {
			if (w < costMatrix.length)
			// if(w < costMatrix.size())
			{
				if (costMatrix[w].length != this.cols)
				// if (costMatrix.get(w).size() != this.cols)
				{
					throw new IllegalArgumentException("Irregular cost matrix");
				}
				this.costMatrix[w] = Arrays.copyOf(costMatrix[w], this.dim);
//               while(costMatrix.get(w).size() < this.dim){
//                   costMatrix.get(w).add(0.0);
//               }
//               this.costMatrix.set(w, costMatrix.get(w));
			} else {
				this.costMatrix[w] = new double[this.dim];
				// this.costMatrix.set(w,new ArrayList<Double>(this.dim));
			}
		}
		labelByWorker = new double[this.dim];
		labelByJob = new double[this.dim];
		minSlackWorkerByJob = new int[this.dim];
		minSlackValueByJob = new double[this.dim];
		committedWorkers = new boolean[this.dim];
		parentWorkerByCommittedJob = new int[this.dim];
		matchJobByWorker = new int[this.dim];
		Arrays.fill(matchJobByWorker, -1);
		matchWorkerByJob = new int[this.dim];
		Arrays.fill(matchWorkerByJob, -1);
	}

	protected void computeInitialFeasibleSolution() {
		for (int j = 0; j < dim; j++) {
			labelByJob[j] = Double.POSITIVE_INFINITY;
		}
		for (int w = 0; w < dim; w++) {
			for (int j = 0; j < dim; j++) {
				if (costMatrix[w][j] < labelByJob[j])
				// if(costMatrix.get(w).get(j) < labelByJob[j])
				{
					labelByJob[j] = costMatrix[w][j];
					// labelByJob[j] = costMatrix.get(w).get(j);
				}
			}
		}
	}

	public int[] execute() {
		/*
		 * Heuristics to improve performance: Reduce rows and columns by their smallest
		 * element, compute an initial non-zero dual feasible solution and create a
		 * greedy matching from workers to jobs of the cost matrix.
		 */
		//System.out.println("execute function");
		reduce();
		//System.out.println("reduce completed");
		computeInitialFeasibleSolution();
		//System.out.println("compute intial feasible solution completed");
		greedyMatch();
		//System.out.println("greedy match completed");
		int w = fetchUnmatchedWorker();
		//System.out.println("fetch unmatched worker completed");
		while (w < dim) {
			initializePhase(w);
			executePhase();
			w = fetchUnmatchedWorker();
		}
		//System.out.println("while loop completed");
		int[] result = Arrays.copyOf(matchJobByWorker, rows);
		for (w = 0; w < result.length; w++) {
			if (result[w] >= cols) {
				result[w] = -1;
			}
		}
		return result;
	}

	protected void executePhase() {
		while (true) {
			int minSlackWorker = -1, minSlackJob = -1;
			double minSlackValue = Double.POSITIVE_INFINITY;
			for (int j = 0; j < dim; j++) {
				if (parentWorkerByCommittedJob[j] == -1) {
					if (minSlackValueByJob[j] < minSlackValue) {
						minSlackValue = minSlackValueByJob[j];
						minSlackWorker = minSlackWorkerByJob[j];
						minSlackJob = j;
					}
				}
			}
			if (minSlackValue > 0) {
				updateLabeling(minSlackValue);
			}
			parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
			if (matchWorkerByJob[minSlackJob] == -1) {
				/*
				 * An augmenting path has been found.
				 */
				int committedJob = minSlackJob;
				int parentWorker = parentWorkerByCommittedJob[committedJob];
				while (true) {
					int temp = matchJobByWorker[parentWorker];
					match(parentWorker, committedJob);
					committedJob = temp;
					if (committedJob == -1) {
						break;
					}
					parentWorker = parentWorkerByCommittedJob[committedJob];
				}
				return;
			} else {
				/*
				 * Update slack values since we increased the size of the committed workers set.
				 */
				int worker = matchWorkerByJob[minSlackJob];
				committedWorkers[worker] = true;
				for (int j = 0; j < dim; j++) {
					if (parentWorkerByCommittedJob[j] == -1) {
						double slack = costMatrix[worker][j] - labelByWorker[worker] - labelByJob[j];
//                        double slack = costMatrix.get(worker).get(j) - labelByWorker[worker] - labelByJob[j];
						if (minSlackValueByJob[j] > slack) {
							minSlackValueByJob[j] = slack;
							minSlackWorkerByJob[j] = worker;
						}
					}
				}
			}
		}
	}

	protected int fetchUnmatchedWorker() {
		int w;
		for (w = 0; w < dim; w++) {
			if (matchJobByWorker[w] == -1) {
				break;
			}
		}
		return w;
	}

	protected void greedyMatch() {
		for (int w = 0; w < dim; w++) {
			for (int j = 0; j < dim; j++) {
				if (matchJobByWorker[w] == -1 && matchWorkerByJob[j] == -1
						&& costMatrix[w][j] - labelByWorker[w] - labelByJob[j] == 0)
//                        && costMatrix.get(w).get(j) - labelByWorker[w] - labelByJob[j] == 0)
				{
					match(w, j);
				}
			}
		}
	}

	protected void initializePhase(int w) {
		Arrays.fill(committedWorkers, false);
		Arrays.fill(parentWorkerByCommittedJob, -1);
		committedWorkers[w] = true;
		for (int j = 0; j < dim; j++) {
			minSlackValueByJob[j] = costMatrix[w][j] - labelByWorker[w] - labelByJob[j];
			// minSlackValueByJob[j] = costMatrix.get(w).get(j) - labelByWorker[w] -
			// labelByJob[j];
			minSlackWorkerByJob[j] = w;
		}
	}

	protected void match(int w, int j) {
		matchJobByWorker[w] = j;
		matchWorkerByJob[j] = w;
	}

	protected void reduce() {
		//System.out.println("inside reduce");
		for (int w = 0; w < dim; w++) {
			double min = Double.POSITIVE_INFINITY;
			for (int j = 0; j < dim; j++) {
				if (costMatrix[w][j] < min)
//                if(costMatrix.get(w).get(j) < min)
				{
					min = costMatrix[w][j];
					// min = costMatrix.get(w).get(j);
				}
			}
			for (int j = 0; j < dim; j++) {
				costMatrix[w][j] -= min;
				// costMatrix.get(w).set(j, costMatrix.get(w).get(j)-min);
			}
		}
		double[] min = new double[dim];
		for (int j = 0; j < dim; j++) {
			min[j] = Double.POSITIVE_INFINITY;
		}
		for (int w = 0; w < dim; w++) {
			for (int j = 0; j < dim; j++) {
				if (costMatrix[w][j] < min[j])
				// if(costMatrix.get(w).get(j) < min[j])
				{
					min[j] = costMatrix[w][j];
					// min[j] = costMatrix.get(w).get(j);
				}
			}
		}
		for (int w = 0; w < dim; w++) {
			for (int j = 0; j < dim; j++) {
				costMatrix[w][j] -= min[j];
				// costMatrix.get(w).set(j, costMatrix.get(w).get(j)-min[j]);
			}
		}
	}

	protected void updateLabeling(double slack) {
		for (int w = 0; w < dim; w++) {
			if (committedWorkers[w]) {
				labelByWorker[w] += slack;
			}
		}
		for (int j = 0; j < dim; j++) {
			if (parentWorkerByCommittedJob[j] != -1) {
				labelByJob[j] -= slack;
			} else {
				minSlackValueByJob[j] -= slack;
			}
		}
	}

}