package com.autoadda.apis.part.request;

import java.util.List;

public class ProductAttributeListPayload {
	private List<ProductAttributeRequest> productAttributeRequestList;

	public List<ProductAttributeRequest> getProductAttributeRequestList() {
		return productAttributeRequestList;
	}

	public void setProductAttributeRequestList(List<ProductAttributeRequest> productAttributeRequestList) {
		this.productAttributeRequestList = productAttributeRequestList;
	}

}
