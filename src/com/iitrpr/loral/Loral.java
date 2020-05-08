package com.iitrpr.loral;
import java.util.*;


public class Loral {
	static HashMap<String, DemandNode> demandMap;
	static HashMap<String, ServiceCenter> serviceMap;
	static HashMap<String, HashMap<String,Integer>> outgoingEdgeMap;
	static HashMap<String, HashMap<String,Integer>> incomingEdgeMap;
	static PriorityQueue<DnToScToken> demandNodeProcessQueue;
	static int threshold,bestK;
	static int minCascadeCost;
	int objectiveFunction = 0;
	int checkIndex = 1311;
	// This variable is only for testing.
	// To store the cascade list which gives out the minimum cascade cost.
	static CascadeList finalCascadeList;
	/*
	 *  This method performs the Loral Algorithm.
	 * */
	public void performLoral() {
		//int tokenIndex=1;
		//For loop for demand nodes being unassigned to the service center.
		int noOfTokensExecuted = 0;
		//while(tokenIndex++<checkIndex+1) {
		while(!demandNodeProcessQueue.isEmpty()) {
			// Token to get the service center and demand node with the minimum distance between them.
			DnToScToken token = demandNodeProcessQueue.poll();
			if(token==null || token.demandNode.isAllocated())
				continue;
			// Remove this line if u run it for multiple ratios
//********** System.out.println("Demand node in execution = " + noOfTokensExecuted++);
			// If the service center has the capacity then allocate the demand node to the service center.
			if(!token.serviceCenter.isfull()) {
				// Since the capacity is >0, so the increase in objective function is only because of the distance. 
				objectiveFunction+=token.distance;
				token.serviceCenter.addAllocation(token.demandNode,token.distance);
				updateBoundaryVertices(token.serviceCenter,token.demandNode);
				// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
				if(incomingEdgeMap.get(token.demandNode.dnid)!=null) {
					for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(token.demandNode.dnid).entrySet()) {
						if(!identifyServiceCenter(entry.getKey())) { 
							DemandNode dnode = demandMap.get(entry.getKey());
							// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
							if(dnode.isAllocated())
								updateBoundaryVertices(dnode.allocation,dnode);
						}
					}
				}
			}else {
				finalCascadeList = new CascadeList();
				//Cascading needs to be implemented here..
				int baseObjFn = token.distance + token.serviceCenter.penalty;
				PriorityQueue<BoundaryAndItsObjFn> bestKBoundaryVertices = new PriorityQueue<BoundaryAndItsObjFn>();
				//This hashmap is used to find the best demand node between the service centers
				for(DemandNode dNode : token.serviceCenter.boundaryVertices) {
					// This loop is to add the demand node and service center distance to the Tree Set.
					for(Map.Entry<ServiceCenter, Integer> distanceDetail : dNode.distanceToSC.entrySet()) {
						// There's no point adding something whose distance is greater than the base objective function value
						if(dNode.allocation!=distanceDetail.getKey()) {
							bestKBoundaryVertices.add(new BoundaryAndItsObjFn(dNode.getDistanceToSC(distanceDetail.getKey())-dNode.distanceToAllocatedSC, dNode, distanceDetail.getKey()));
						}
					}
				}
				
				//Initializing it to the base object function to campare it to all the cascading cost.
				minCascadeCost = baseObjFn;
				int k = 0;
				while((!bestKBoundaryVertices.isEmpty()) && (k++ < bestK)) {
					BoundaryAndItsObjFn boundaryVertex = bestKBoundaryVertices.poll();
					
					// This hash set to take care that the service center is not repeated.
					HashSet<ServiceCenter> visitedSC = new HashSet<ServiceCenter>();
					visitedSC.add(token.serviceCenter);
					
					// Since we are breaking the boundary vertex so we are subtracting the distance.
					int cascadeObjFn = token.distance + boundaryVertex.deltaDistance;
					
					// List to store the path through which the cascading proceeds.
					CascadeList currentCascadeDetail = new CascadeList();
					
					cascadePath(cascadeObjFn, currentCascadeDetail, visitedSC, boundaryVertex.serviceCenter, boundaryVertex.demandNode);
				}
				
				// Check if the cascading needs to happen or not.
				if(minCascadeCost<baseObjFn) { 
					// It means that cascading cost is less than the direct allocation of demand to service center.
					token.serviceCenter.addAllocation(token.demandNode,token.distance);
					updateBoundaryVertices(token.serviceCenter,token.demandNode);
					// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
					if(incomingEdgeMap.get(token.demandNode.dnid)!=null) {
						for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(token.demandNode.dnid).entrySet()) {
							if(!identifyServiceCenter(entry.getKey())) { 
								DemandNode dnode = demandMap.get(entry.getKey());
								// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
								if(dnode.isAllocated())
									updateBoundaryVertices(dnode.allocation,dnode);
							}
						}
					}
					performCascading(finalCascadeList);
					objectiveFunction+=minCascadeCost;
				}
				else {
					// It means that the base condition was the perfect choice.
					token.serviceCenter.addAllocation(token.demandNode,token.distance);
					updateBoundaryVertices(token.serviceCenter,token.demandNode);
					// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
					if(incomingEdgeMap.get(token.demandNode.dnid)!=null) {
						for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(token.demandNode.dnid).entrySet()) {
							if(!identifyServiceCenter(entry.getKey())) { 
								DemandNode dnode = demandMap.get(entry.getKey());
								// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
								if(dnode.isAllocated())
									updateBoundaryVertices(dnode.allocation,dnode);
							}
						}
					}
					objectiveFunction += baseObjFn;
				}
			}
		}
		
		//System.out.println("*************The total objective cost is : " + objectiveFunction + "*************");
	}
	
	public int cascadePath(int cascadePathCost, CascadeList cascadeList,HashSet<ServiceCenter> visitedSC, ServiceCenter serviceCenter, DemandNode demandNode) {
		// Cascading happens till the time the visited service center length becomes equal to the threshold.
		if(threshold==0) {
			return Integer.MAX_VALUE;
		}
		// Distance between demand node and service center.
		int distance = demandNode.getDistanceToSC(serviceCenter);
		
		// Update cascade list.
		cascadeList.insertAtEnd(new CascadePath(serviceCenter, demandNode, distance));
		
		if(!serviceCenter.isfull()) {
			copyPathToFinalList(cascadePathCost,cascadeList);
			return cascadePathCost;
		} 
		else if(visitedSC.size() >= threshold) {
			copyPathToFinalList(cascadePathCost + serviceCenter.penalty,cascadeList);
			return cascadePathCost + serviceCenter.penalty;
		}
		else {
			// Adding the service center to the visited service center so that it is not further processed.
			visitedSC.add(serviceCenter);
			//Cascading needs to be implemented here.
			// Base condition to check if we go ahead with the penalty.
			int baseObjFn =  cascadePathCost + serviceCenter.penalty;
			// This loop is to iterate over all the boundary vertices
			BoundaryAndItsObjFn bestBoundaryVertex=null;
			int bestKMin=Integer.MAX_VALUE;
			for(DemandNode boundaryDemandNode : serviceCenter.boundaryVertices) {
				// This loop is to add the demand node and service center distance to the Tree Set.
				for(Map.Entry<ServiceCenter, Integer> distanceDetail : boundaryDemandNode.distanceToSC.entrySet()) {
					if((baseObjFn>distanceDetail.getValue()) && (!visitedSC.contains(distanceDetail.getKey())) && (demandNode.allocation!=distanceDetail.getKey())) {
						int cost = cascadePathCost + distanceDetail.getValue()-boundaryDemandNode.distanceToAllocatedSC;
						if(distanceDetail.getKey().isfull())
							cost += distanceDetail.getKey().penalty;
						if(cost<bestKMin) {
							bestKMin = cost;
							bestBoundaryVertex = new BoundaryAndItsObjFn(distanceDetail.getValue()-boundaryDemandNode.distanceToAllocatedSC, boundaryDemandNode, distanceDetail.getKey());
						}
					}
				}
			}
			
			//while((!bestKBoundaryVertices.isEmpty()) && (iterationStep++ < Loral.bestK)) {
			if(bestBoundaryVertex!=null) {
				// Since we are breaking the boundary vertex so we are subtracting the distance. 
				int cascadeObjFn = cascadePathCost + bestBoundaryVertex.deltaDistance;
				// Cascading Cost Calculation
				cascadeObjFn = cascadePath(cascadeObjFn, cascadeList, visitedSC, bestBoundaryVertex.serviceCenter, bestBoundaryVertex.demandNode);
				// Maintaining the minimum cascading list.
				if(cascadeObjFn<baseObjFn) {
					copyPathToFinalList(cascadeObjFn,cascadeList);
					cascadeList.removeFromIndex(visitedSC.size()-1);
					visitedSC.remove(serviceCenter);
					return cascadeObjFn;
				}else {
					cascadeList.removeFromIndex(visitedSC.size()-1);
					copyPathToFinalList(baseObjFn,cascadeList);
					visitedSC.remove(serviceCenter);
					return baseObjFn;
				}
			}else {
				cascadeList.removeFromIndex(visitedSC.size()-1);
				copyPathToFinalList(baseObjFn,cascadeList);
				visitedSC.remove(serviceCenter);
				return baseObjFn;
			}
		}
	}
		
	public void performCascading(CascadeList cascadeList) {
		for(int i=0; i<cascadeList.size; i++) {
			CascadePath path = cascadeList.list[i];
			// First remove the demand vertex previous allocation
			path.demandNode.allocation.removeAllocation(path.demandNode);
			// Now add allocation to the new service center
			path.serviceCenter.addAllocation(path.demandNode,path.distance);
			// Update the boundary vertices
			updateBoundaryVertices(path.serviceCenter,path.demandNode);
			// Now we are checking if the incoming demand nodes to the token demand node has become boundary vertices or not.
			if(incomingEdgeMap.get(path.demandNode.dnid)!=null) {
				for(Map.Entry<String, Integer> entry : incomingEdgeMap.get(path.demandNode.dnid).entrySet()) {
					if(!identifyServiceCenter(entry.getKey())) { 
						DemandNode dnode = demandMap.get(entry.getKey());
						// If the demand node is allocated to a service center then only we require to check whether it is a boundary vertices or not.
						if(dnode.isAllocated())
							updateBoundaryVertices(dnode.allocation,dnode);
					}
				}
			}
		}
	}
	
	/*
	 * Boundary Vertices has at-least one of the following three properties:
	 * (a) an outgoing edge to a vertex allotted to a different service center sj
	 * (b) an outgoing edge to a different service center sj
	 * (c) an outgoing edge to an unprocessed demand vertex
	 * 
	 * */
	public void updateBoundaryVertices(ServiceCenter serviceCenter,DemandNode demandNode) {
		//Get the ID of the node:
		HashMap<String,Integer> idMap = outgoingEdgeMap.get(demandNode.dnid);
		boolean isBoundaryVertices = false;
		for(Map.Entry<String, Integer> idVal : idMap.entrySet()) {
			//Identifying whether the outgoing edge is to service center or demand node
			boolean isServiceCenter = identifyServiceCenter(idVal.getKey());
			//if it is a service center the case(b) needs to be identified.
			if(isServiceCenter && serviceMap.get(idVal.getKey())!=serviceCenter) {
				// it means that the outgoing edge is going to the different service center. So, Boundary vertex.
				isBoundaryVertices=true;
				break;
			}
			else if(!isServiceCenter) {
			// It's a demand node so condition needs to be verified.
				DemandNode outgoingDemandNode = demandMap.get(idVal.getKey());
				//Case C verification.
				if(!outgoingDemandNode.isAllocated()) {
				// the outgoing edge to the demand node id not allocated. So, Boundary Vertex
					isBoundaryVertices=true;
					break;
				}
				//Case A verification
				else {
					if(outgoingDemandNode.allocation!=serviceCenter) {
					// it means that the outgoing node is allocated to the different service center. So, Boundary vertex.
						isBoundaryVertices=true;
						break;
					}
				}
			}
		}
		if(isBoundaryVertices) 
			serviceCenter.boundaryVertices.add(demandNode);
		else if(serviceCenter.boundaryVertices.contains(demandNode))
			serviceCenter.boundaryVertices.remove(demandNode);
	}
	
	public boolean identifyServiceCenter(String key){
		if(serviceMap.containsKey(key))
			return true;
		else
			return false;
	}
	
	public void printAllInformation() {
		System.out.println("------------------------------------------------");
		for(Map.Entry<String, ServiceCenter> entry : serviceMap.entrySet()) {
			System.out.println("Service Center : " + entry.getKey() + " Current Capacity : " + entry.getValue().curCapacity + " Penalty : " + entry.getValue().penalty);
			for(DemandNode demandNode : entry.getValue().allocations) {
				System.out.println("Demand Node Allocated : " + demandNode.dnid);
			}
			System.out.println("Boundary Vertices :");
			for(DemandNode demandNode : entry.getValue().boundaryVertices) {
				System.out.println("Boundary Vertex : " + demandNode.dnid);
			}
			System.out.println("\n------------------------------------------------");
		}
	}
	
	public static void copyPathToFinalList(int cascadeObjFn, CascadeList list) {
		//System.out.println("copyPathToFinalList initiated : cascadeObjFn = " + cascadeObjFn + " minCostAcrossAllThread = " + minCostAcrossAllThreads);
		if(cascadeObjFn<minCascadeCost) {
			minCascadeCost = cascadeObjFn;
			finalCascadeList.size=0;
			for(int i=0;i<list.size;i++) 
				finalCascadeList.insertAtEnd(list.list[i]);
		}
	}
	
}
