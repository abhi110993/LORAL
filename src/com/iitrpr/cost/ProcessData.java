package com.iitrpr.cost;

import java.io.*;
import java.util.*;


public class ProcessData {
	
	static int noOfNodes, maxValue, noOfSC;
	static HashMap<String, Integer> nodesIndexMap;
	static int[][] nXnMatrix;
	static String[] serviceNodes;
	static float ratioTotalCapacityToDemandNode = 0.5f;
	static ArrayList<String> nodes;
	
	
	public static void main(String[] args) throws IOException{
		readFileAndSetIndex();
		System.out.println("Index setup completed");
		constructNxNmatrix();
		System.out.println("N x N matrix created");
		applyAllPairShortestPathAlgorithm();
		System.out.println("All pair shortest path is applied");
		//printNxNmatrix();
		
		int[] ratioDemandToService = {300};
		//int[] ratioDemandToService = {2};
		for(int ratio : ratioDemandToService) {
	       	//This would take the ratio and all other details to prepare the service nodes
			prepareServiceNodes(ratio);
			// You can also give your service centers manually. Check giveServiceNodesManually function.
			//giveServiceNodesManually();
			System.out.println("Service Nodes prepared");
			String path = "./dataset/"+ratio+"/ServiceCenter.txt";
			int capacity = Math.round(((noOfNodes-noOfSC)*ratioTotalCapacityToDemandNode)/noOfSC);
			//int penaltyRange = (int)(Math.pow(10, (int)Math.log10(noOfNodes)-1));
			int penaltyRange = 200;
			System.out.println("Penalty Range : " + penaltyRange);
			saveServiceNodesToFile(capacity,penaltyRange,path);
			// You can also save your service centers manually. Check saveServiceNodesToFileManually function.
			//saveServiceNodesToFileManually(path);
			System.out.println("Service Nodes are written to file");
			path = "./dataset/"+ratio+"/CostMatrix.txt";
			saveDistanceMatrixToFile(path);
			System.out.println("Distance matrix has been saved for ratio = " + ratio);
		}
	}
	
	static void saveDistanceMatrixToFile(String path) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		String line = "";
		int distance=0;
		for(String node : nodes) {
			line = "";
			for(String sc : serviceNodes) {
				distance = nXnMatrix[nodesIndexMap.get(node)][nodesIndexMap.get(sc)];
				if(distance<maxValue) 
					line += distance + ",";
				else
					line += "Infinite" + ",";
			}
			bw.write(line);
			bw.newLine();
		}
		bw.close();
	}
	
	static void saveServiceNodesToFileManually(String path) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write("S1,4,10");
		bw.newLine();
		bw.write("S2,2,10");
		bw.newLine();
		bw.write("S3,3,10");
		bw.newLine();
		bw.write("S4,1,8");
		bw.newLine();
		bw.write("S5,1,6");
		bw.newLine();
		bw.close();
	}
	
	static void saveServiceNodesToFile(int capacity, int penaltyRange, String path) throws IOException{
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		Random random = new Random();
		int penalty=0;
		
		for(String sc : serviceNodes) {
			penalty = (random.nextInt(penaltyRange/2)+penaltyRange/2);
			String line = sc + "," + capacity + "," + penalty;
			bw.write(line);
			bw.newLine();
		}
		bw.close();
	}
	
	static void prepareServiceNodes(int ratioDtoS) {
		// This equation comes after considering that #DemandNode = #Nodes - #ServiceCenter  
		noOfSC=((noOfNodes)/(ratioDtoS+1));
		serviceNodes = new String[noOfSC];
		int proportion = ratioDtoS + 1;
		for(int i = 0;i<noOfSC;i++) {
			serviceNodes[i] = nodes.get(i*proportion);
		}
	}
	
	static void giveServiceNodesManually(){
		serviceNodes = new String[5];
		serviceNodes[0] = "S1";
		serviceNodes[1] = "S2";
		serviceNodes[2] = "S3";
		serviceNodes[3] = "S4";
		serviceNodes[4] = "S5";
	}
	
	
	static void printNxNmatrix(){
		for(int i=0;i<noOfNodes;i++) {
			for(int j=0;j<noOfNodes;j++) 
				System.out.print(" " + nXnMatrix[i][j]);
			System.out.println();
		}
	}
	
	static void applyAllPairShortestPathAlgorithm() {
		for(int k=0;k<noOfNodes;k++) {
			for(int i=0;i<noOfNodes;i++) {
				for(int j=0;j<noOfNodes;j++) {
					if(nXnMatrix[i][j]>nXnMatrix[i][k]+nXnMatrix[k][j])
						nXnMatrix[i][j]=nXnMatrix[i][k]+nXnMatrix[k][j];
				}
			}
			System.out.println("Nodes Processed : " + k);
		}
	}
	
	static void constructNxNmatrix() throws IOException{
		nXnMatrix = new int[noOfNodes][noOfNodes];
		maxValue = 50000;//Empirical Value taken for denoting infinite 
		for(int i = 0;i<noOfNodes;i++) {
			for(int j = 0;j<noOfNodes;j++) 
				nXnMatrix[i][j] = maxValue;
		}
		
		BufferedReader br = new BufferedReader(new FileReader("./dataset/edges.txt"));
		String line = "";
		while(((line = br.readLine())!=null) && (!line.equals(""))) {
			String[] st = line.trim().split(",");
			int val = Integer.parseInt(st[2]);
			nXnMatrix[nodesIndexMap.get(st[0])][nodesIndexMap.get(st[1])] = val;
		}
		br.close();
	}
	
	static void readFileAndSetIndex() throws IOException{
		nodesIndexMap = new HashMap<String, Integer>();
		nodes = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader("./dataset/nodes.txt"));
		String line = "";
		int i=0;String val;
		while(((line = br.readLine())!=null) && (!line.equals(""))) {
			noOfNodes++;
			val = line.trim();
			nodesIndexMap.put(val,i++);
			nodes.add(val);
		}
		br.close();
	}
}
