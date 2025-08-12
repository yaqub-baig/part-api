package com.autoadda.apis.part.request;

import java.util.List;

public class CategoryListPayload {
	private List<CategoryRequest> categoryRequestList;

	public List<CategoryRequest> getCategoryRequestList() {
		return categoryRequestList;
	}

	public void setCategoryRequestList(List<CategoryRequest> categoryRequestList) {
		this.categoryRequestList = categoryRequestList;
	}

}
