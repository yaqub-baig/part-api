package com.platform.parts.request;

import java.util.List;

public class CategoryAttributeListPayload {
    private List<CategoryAttributeRequest> categoryAttributeRequestList;

	public List<CategoryAttributeRequest> getCategoryAttributeRequestList() {
		return categoryAttributeRequestList;
	}

	public void setCategoryAttributeRequestList(List<CategoryAttributeRequest> categoryAttributeRequestList) {
		this.categoryAttributeRequestList = categoryAttributeRequestList;
	}

}
