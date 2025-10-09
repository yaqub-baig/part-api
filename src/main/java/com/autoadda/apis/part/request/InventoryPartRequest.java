package com.autoadda.apis.part.request;

import com.autoadda.apis.part.util.ProductCondition;
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
	private String partNumber;
	private Integer quantityInStock;
	private Double unitPrice;
	private String location;
	private ProductCondition condition;
	private String sku;
	private Boolean isActive;
}