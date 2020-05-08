
package com.iitrpr.hungarian;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PreProcessor {
	private String fileName;
	private double[][] costMatrix;
	// private ArrayList<ArrayList<Double>> costMatrix;
	private int demandNodes;
	private int serviceCenters;
	int maxDistance;
	private int total;
	private String[] demandNodesLabels;
	private String[] serviceCenterLabels;

	public PreProcessor(String fileName) {
		this.fileName = fileName;
		this.total = 0;
		this.demandNodes = 0;
		this.serviceCenters = 0;
	}

	public int getTotalNoOfDemandNodes() {
		return this.demandNodes;
	}

	public int getTotalNoOfServiceCenters() {
		return this.serviceCenters;
	}

	public int getDummyNodes() {
		return (this.demandNodes * this.serviceCenters) - this.demandNodes;
	}

	public double getDummyWeight() {
		return maxDistance;
	}

	public int getTotal() {
		return total;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String[] getDemandNodeLabels() {
		return this.demandNodesLabels;
	}

	public String[] getServiceCenterLabels() {
		return this.serviceCenterLabels;
	}

	public void fillCostMatrixForISC(int distance, int penalty, int capacity, int ithDemandNode, int jthServiceCenter) {
		int startIndex = (jthServiceCenter) * this.demandNodes;
		int endIndex = this.demandNodes + startIndex - 1;

		for (int j = startIndex; j <= endIndex; j++) {
			if (j % demandNodes < capacity) {
				costMatrix[ithDemandNode][j] = distance;
				// costMatrix.get(ithDemandNode).set(j,(double)distance);
			} else {
				costMatrix[ithDemandNode][j] = distance + penalty;
				// costMatrix.get(ithDemandNode).set(j, (double)(distance+penalty));
			}
		}
	}

	public double[][] getCostMatrix() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(this.fileName + "ServiceCenter.txt"));
		ArrayList<String> serviceCenter = new ArrayList<String>();
		ArrayList<Integer> penaltyCost = new ArrayList<Integer>();
		ArrayList<Integer> capacity = new ArrayList<Integer>();

		String line = "";
		while ((line = br.readLine()) != null) {
			String st[] = line.split(",");
			serviceCenter.add(st[0]);
			capacity.add(Integer.parseInt(st[1]));
			penaltyCost.add(Integer.parseInt(st[2]));
		}
		br.close();
		HashMap<Integer, String> nodeMap = new HashMap<Integer, String>();
		br = new BufferedReader(new FileReader(this.fileName + "nodes.txt"));
		line = "";
		int i = 0;
		while ((line = br.readLine()) != null) {
			nodeMap.put(i++, line.trim());
		}
		br.close();
		this.demandNodes = nodeMap.size() - serviceCenter.size();
		this.serviceCenters = serviceCenter.size();
		this.total = demandNodes * serviceCenters;
		this.costMatrix = new double[total][total];
		this.demandNodesLabels = new String[this.demandNodes];
		this.serviceCenterLabels = new String[this.serviceCenters];
		for (i = 0; i < serviceCenters; i++) {
			this.serviceCenterLabels[i] = serviceCenter.get(i);
		}
		br = new BufferedReader(new FileReader(this.fileName + "CostMatrix.txt"));
		maxDistance = 0;
		int k = 0;
		for (i = 0; i < nodeMap.size(); i++) {
			if (serviceCenter.contains(nodeMap.get(i))) {
				k++;
				continue;
			}
			String[] distanceVector = br.readLine().split(",");
			this.demandNodesLabels[i - k] = nodeMap.get(i);
			for (int j = 0; j < serviceCenters; j++) {
				int distance = Integer.parseInt(distanceVector[j]);
				int penalty = penaltyCost.get(j);

				if (maxDistance < distance + penalty) {
					maxDistance = distance + penalty;
				}
				fillCostMatrixForISC(distance, penalty, capacity.get(j), i-k, j);
			}
		}
		br.close();
		maxDistance++;
		int dummyNodeIndex = demandNodes;
		for (i = dummyNodeIndex; i < total; i++) {
			for (int j = 0; j < total; j++) {
				costMatrix[i][j] = maxDistance;
			}
		}
		return this.costMatrix;
	}

	public void printMatrix() {
		for (int i = 0; i < total; i++) {
			for (int j = 0; j < total; j++) {
				System.out.print(costMatrix[i][j] + " ");
			}
			System.out.println();
		}
	}
}