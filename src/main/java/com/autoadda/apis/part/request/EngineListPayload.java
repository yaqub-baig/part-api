package com.autoadda.apis.part.request;

import java.util.List;

public class EngineListPayload {
	private List<EngineRequest> engineRequestList;

	public List<EngineRequest> getEngineRequestList() {
		return engineRequestList;
	}

	public void setEngineRequestList(List<EngineRequest> engineRequestList) {
		this.engineRequestList = engineRequestList;
	}
}
