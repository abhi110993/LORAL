package com.iitrpr.cost;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class PrepareServiceNodes {
	
	static int noOfNodes=23839;
	static int noOfSC;
	static HashMap<String, Integer> nodesIndexMap;
	static float ratioTotalCapacityToDemandNode = 0.7f;
	static ArrayList<String> nodes;
	static int[] capacities;
	static int penaltyRange = 50;
	static Random random = new Random();
	public static void main(String[] args) throws Exception{
		//int[] ratioDemandToService = {400,500,700};
		int ratioDemandToService = 400;
		noOfSC = ((noOfNodes) / (ratioDemandToService + 1));
		String path = "./Resource/ServiceCenter.txt";
		int totalCap = Math.round((noOfNodes - noOfSC) * ratioTotalCapacityToDemandNode);
		System.out.println(totalCap);
		capacities = new int[noOfSC];
		setupCapacities(true, totalCap);
		saveServiceNodesToFile(penaltyRange, path);
		System.out.println("Service Nodes are written to file");
	}
	
	static void setupCapacities(boolean random,int totalCap) {
		if(random) {
			HashSet<Integer> set = new HashSet<Integer>();
			for(int i=0;i<noOfSC-1;i++) {
				int randVal = giveRandomValue(totalCap);
				while(set.contains(randVal))
					randVal = giveRandomValue(totalCap);
				set.add(randVal);
			}
			int[] partition = new int[noOfSC-1];
			int i=0;
			for(int val : set) 
				partition[i++] = val;
			Arrays.parallelSort(partition);
			i=0;
			int prev = 0;
			for(int val : partition) {
				capacities[i++] = val-prev;
				prev = val;
			}
			capacities[i] = totalCap-prev;
		}
		else {
			for(int i=0;i<noOfSC;i++)
				capacities[i] = totalCap/noOfSC;
		}
	}
	
	static void saveServiceNodesToFile(int penaltyRange, String path) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		String[] serviceNodes = new String[noOfSC];
		for(int i = 0;i<noOfSC;i++) {
			line = br.readLine();
			serviceNodes[i] = line.split(",")[0];
		}
		br.close();
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		int penalty=0;
		for(int i=0;i<noOfSC;i++) {
			penalty = giveRandomValue(penaltyRange);
			line = serviceNodes[i] + "," + capacities[i] + "," + penalty;
			bw.write(line);
			bw.newLine();
		}
		bw.close();
	}
	
	static int giveRandomValue(int range) {
		return (random.nextInt(range)+1);
	}
}
