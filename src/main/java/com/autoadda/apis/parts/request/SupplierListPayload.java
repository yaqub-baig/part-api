package com.autoadda.apis.parts.request;

import java.util.List;

public class SupplierListPayload {
	private List<SupplierRequest> supplierRequestList;

	public List<SupplierRequest> getSupplierRequestList() {
		return supplierRequestList;
	}

	public void setSupplierRequestList(List<SupplierRequest> supplierRequestList) {
		this.supplierRequestList = supplierRequestList;
	}


}
