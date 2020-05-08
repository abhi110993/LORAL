package com.iitrpr.loral;
import java.io.*;
import java.util.HashMap;


public class Simulator {

	public static void main(String[] args) throws IOException{
		//int[] demandToScRatio = {400, 500,600,700 };
		int[] demandToScRatio = {300,400};
		for(int ratio : demandToScRatio) {
			System.out.println("***********************************************************");
			PreProcessor.ratio=ratio;
			PreProcessor preprocess = new PreProcessor();
			Loral loral = new Loral();
			Loral.demandMap = new HashMap<String, DemandNode>();
			Loral.serviceMap = new HashMap<String, ServiceCenter>();
			Loral.outgoingEdgeMap = new HashMap<String, HashMap<String,Integer>>();
			Loral.incomingEdgeMap = new HashMap<String, HashMap<String,Integer>>();
			// Order of loading service center first and then demand node must not be changed.
			preprocess.loadServiceCenter();
			preprocess.loadDemandNode();
			preprocess.loadEdges();
			preprocess.distanceMatrixToDemandNodes();
			// Threshold is for limiting the cascading length
			Loral.threshold = Loral.serviceMap.size();
			// BestK is for limiting the boundary nodes to service node pairs for cascading. 
			Loral.bestK=Loral.serviceMap.size();
			//Loral.bestK=Integer.MAX_VALUE;
			double startTime = System.nanoTime();
			
			loral.performLoral();
			
			double endTime = System.nanoTime();
			// This will print all the allocation which the service center has attained.
			//loral.printAllInformation();
			System.out.println("Total Execution time in ns = " + (endTime - startTime));
			System.out.println("Total Objective Function = " + loral.objectiveFunction);
		}
		
		
	} 

}