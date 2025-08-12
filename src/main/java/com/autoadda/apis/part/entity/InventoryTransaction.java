package com.autoadda.apis.part.entity;

import java.time.LocalDateTime;

import com.autoadda.apis.part.util.TransactionType;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private InventoryPart part;

	private int quantity;

	@Enumerated(EnumType.STRING)
	private TransactionType transactionType;

	private String referenceNote; // e.g., "Purchase Order #123", "Used in JobCard 456"

	private LocalDateTime timestamp;

}
