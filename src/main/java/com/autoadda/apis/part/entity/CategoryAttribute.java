package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "category_attribute")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class CategoryAttribute {

    @Id
    @Column(name = "id")
    private String id;

    //	@ManyToOne
    @Column(name = "category_id")
    private String categoryId;

    //    @ManyToOne
    @Column(name = "attribute_id")
    private String attributeId;
}
