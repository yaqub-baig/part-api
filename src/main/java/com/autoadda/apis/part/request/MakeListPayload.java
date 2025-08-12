package com.autoadda.apis.part.request;

import java.util.List;

public class MakeListPayload {
	private List<MakeRequest> makeRequestList;

	public List<MakeRequest> getMakeRequestList() {
		return makeRequestList;
	}

	public void setMakeRequestList(List<MakeRequest> makeRequestList) {
		this.makeRequestList = makeRequestList;
	}

	
}
