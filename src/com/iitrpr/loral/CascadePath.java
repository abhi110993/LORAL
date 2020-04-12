package com.iitrpr.loral;

public class CascadePath {
	
	public ServiceCenter serviceCenter;
	public DemandNode demandNode;
	// Remove this if not used - redundancy
	public int distance;
	
	public CascadePath(ServiceCenter sc, DemandNode dn, int d) {
		serviceCenter = sc;
		demandNode = dn;
		this.distance = d;
	}
}
