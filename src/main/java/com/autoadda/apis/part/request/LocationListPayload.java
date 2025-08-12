package com.autoadda.apis.part.request;

import java.util.List;

public class LocationListPayload {
	private List<LocationRequest> locationRequestList;

	public List<LocationRequest> getLocationRequestList() {
		return locationRequestList;
	}

	public void setLocationRequestList(List<LocationRequest> locationRequestList) {
		this.locationRequestList = locationRequestList;
	}

}
