package com.autoadda.apis.parts.entity;

import java.time.LocalDateTime;

import com.autoadda.apis.parts.util.TransactionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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
