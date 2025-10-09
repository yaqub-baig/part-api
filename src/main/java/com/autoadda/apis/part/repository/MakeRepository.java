package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.Make;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MakeRepository extends JpaRepository<Make, String> {

    Make findByName(String name);
}
