package com.autoadda.apis.part.entity;

import lombok.*;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;


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

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "category_id")
    @Builder.Default
    private List<MediaFile> images = new ArrayList<> ( );

}
