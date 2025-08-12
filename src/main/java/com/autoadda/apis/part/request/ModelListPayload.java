package com.autoadda.apis.part.request;

import java.util.List;

public class ModelListPayload {
	private List<ModelRequest> modelRequestList;

	public List<ModelRequest> getModelRequestList() {
		return modelRequestList;
	}

	public void setModelRequestList(List<ModelRequest> modelRequestList) {
		this.modelRequestList = modelRequestList;
	}

}
