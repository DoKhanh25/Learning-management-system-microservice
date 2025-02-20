package com.example.userservice.repository;

import com.example.userservice.entity.CohortEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CohortRepository extends JpaRepository<CohortEntity, Long> {
//    @Query(value = "select c from cohort c")
//    public List<CohortEntity> getAllCohorts();
//
//    @Query(value = "select c from cohort c where c.id = ?1")
//    public CohortEntity getCohortById(Long id);

    public Optional<CohortEntity> findCohortEntityByName(String name);

}
