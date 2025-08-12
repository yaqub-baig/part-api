package com.autoadda.apis.part.request;

import java.util.List;

public class VehicleTypeListPayload {
	private List<VehicleTypeRequest> vehicleTypeRequestList;

	public List<VehicleTypeRequest> getVehicleTypeRequestList() {
		return vehicleTypeRequestList;
	}

	public void setVehicleTypeRequestList(List<VehicleTypeRequest> vehicleTypeRequestList) {
		this.vehicleTypeRequestList = vehicleTypeRequestList;
	}

}
