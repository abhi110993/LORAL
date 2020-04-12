package com.iitrpr.loral;
import java.util.*;

public class DemandNode{
	public String dnid;
	public ServiceCenter allocation;
	public int distanceToAllocatedSC;
	//Redundancy: Remove it if not used
	public HashMap<ServiceCenter, Integer> distanceToSC;
	
	public DemandNode(String dnid, ServiceCenter allocation) {
		this.dnid = dnid;
		this.allocation = allocation;
		distanceToSC = new HashMap<ServiceCenter, Integer>();
	}
	
	public void addDistanceToSC(int d, ServiceCenter sc) {
		distanceToSC.put(sc,d);
	}
	
	public int getDistanceToSC(ServiceCenter sc) {
		return distanceToSC.get(sc);
	}
	
	public boolean isAllocated() {
		if(allocation==null)
			return false;
		else
			return true;
	}
	
	public void assignAllocation(ServiceCenter sc) {
		allocation = sc;
	}
	
}
