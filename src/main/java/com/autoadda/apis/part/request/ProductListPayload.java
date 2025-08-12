package com.autoadda.apis.part.request;

import java.util.List;

public class ProductListPayload {
	private List<ProductRequest> productRequestList;

	public List<ProductRequest> getProductRequestList() {
		return productRequestList;
	}

	public void setProductRequestList(List<ProductRequest> productRequestList) {
		this.productRequestList = productRequestList;
	}

}
