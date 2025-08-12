package com.autoadda.apis.part.request;

import java.util.List;

public class AttributeListPayload {
	private List<AttributeRequest> attributeRequestList;

	public List<AttributeRequest> getAttributeRequestList() {
		return attributeRequestList;
	}

	public void setAttributeRequestList(List<AttributeRequest> attributeRequestList) {
		this.attributeRequestList = attributeRequestList;
	}

}
