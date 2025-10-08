package com.autoadda.apis.part.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Data
public class InventoryPartRequest {

	private String productId;
	private String supplierId;
	private String partName;
	private String partNumber;
	private String category;
	private String manufacturer;
	private Integer quantityInStock;
	private Double unitPrice;
	private Integer quantity;
	private String location;
	private Boolean isActive;
}