package com.iitrpr.loral;
import java.util.HashSet;

public class ServiceCenter {
	public int penalty;
	public String scid;
	public int curCapacity;
	public HashSet<DemandNode> allocations;
	
	/*
	 * Boundary Vertices has at-least one of the following three properties:
	 * (a) an outgoing edge to a vertex allotted to a different service center sj
	 * (b) an outgoing edge to a different service center sj
	 * (c) an outgoing edge to an unprocessed demand vertex
	 * 
	 * */
	public HashSet<DemandNode> boundaryVertices;
	
	/**
	 * @param Penalty
	 * @param scid
	 * @param maxCap
	 * */
	public ServiceCenter(String scid, int maxCap, int penalty) {
		this.penalty = penalty;
		this.scid = scid;
		this.curCapacity = maxCap;
		allocations = new HashSet<DemandNode>();
		boundaryVertices = new HashSet<DemandNode>();
	}
	
	public void addAllocation(DemandNode demandNode, int distance) {
		allocations.add(demandNode);
		curCapacity--;
		demandNode.assignAllocation(this);
		demandNode.distanceToAllocatedSC = distance;
	}
	
	public void removeAllocation(DemandNode demandNode) {
		allocations.remove(demandNode);
		curCapacity++;
		demandNode.assignAllocation(null);
		demandNode.distanceToAllocatedSC = Integer.MAX_VALUE;
		if(boundaryVertices.contains(demandNode))
			boundaryVertices.remove(demandNode);
	}
	
	public boolean isfull() {
		if(curCapacity<1)
			return true;
		else 
			return false;
	}
}
