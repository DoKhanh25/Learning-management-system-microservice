package com.example.userservice.repository;

import com.example.userservice.entity.CohortMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CohortMemberRepository extends JpaRepository<CohortMemberEntity, Long> {
    @Query("SELECT cm FROM cohort_member cm where cm.cohort.id = :id")
    public List<CohortMemberEntity> getCohortMemberEntitiesByCohortId(@Param("id") Long id);
}
