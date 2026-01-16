package com.autoadda.apis.part.entity;

import com.autoadda.apis.part.util.Status;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer vehicleOwnerId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<> ( );

    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private Status status; // e.g., CREATED, SHIPPED, DELIVERED
}
