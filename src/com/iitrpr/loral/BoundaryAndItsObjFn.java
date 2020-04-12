package com.iitrpr.loral;

public class BoundaryAndItsObjFn implements Comparable<BoundaryAndItsObjFn>{
	public int deltaDistance;
	public DemandNode demandNode;
	public ServiceCenter serviceCenter;
	
	public BoundaryAndItsObjFn(int deltaDistance,DemandNode dn,ServiceCenter sc) {
		this.deltaDistance = deltaDistance;
		demandNode = dn;
		serviceCenter = sc;
	}
	
	public int compareTo(BoundaryAndItsObjFn obj) {
		return this.deltaDistance - obj.deltaDistance;
	}
}
