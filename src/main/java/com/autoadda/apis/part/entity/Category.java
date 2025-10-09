package com.autoadda.apis.part.entity;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "category")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data

public class Category {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    //	@ManyToOne
    @Column(name = "parent_category_id")
    private String parentCategoryId;

//	@OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL)
//	private Set<PartCategory> subCategories;

    @PrePersist
    public void generateId() {
        if ( this.id == null || this.id.isEmpty ( ) ) {
            this.id = UUID.randomUUID ( ).toString ( );
        }
    }

}
