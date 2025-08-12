package com.autoadda.apis.part.request;

import java.util.List;

public class ManufacturerListPayload {
	private List<ManufacturerRequest> manufacturerRequestList;

	public List<ManufacturerRequest> getManufacturerRequestList() {
		return manufacturerRequestList;
	}

	public void setManufacturerRequestList(List<ManufacturerRequest> manufacturerRequestList) {
		this.manufacturerRequestList = manufacturerRequestList;
	}
}
