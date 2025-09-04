package com.autoadda.apis.part.repository;

import com.autoadda.apis.part.entity.BrandOnboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandOnboardRepository extends JpaRepository<BrandOnboard,Integer>{
    @Query("SELECT b.requestReferenceId FROM BrandOnboard b WHERE b.requestReferenceId LIKE :prefix% ORDER BY b.requestReferenceId DESC")
    List<String> findLatestCodesByPrefix(@Param("prefix")String codePrefix);

    @Query("SELECT b.requestReferenceId FROM BrandOnboard b WHERE b.requestReferenceId LIKE %:prefix% ORDER BY b.requestReferenceId DESC")
    List<String> findAllCodesForGlobalSequence(@Param("prefix") String prefix);

    Optional<BrandOnboard> findByRequestReferenceId(String referenceId);
}
