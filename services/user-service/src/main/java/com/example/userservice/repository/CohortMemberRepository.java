package com.example.userservice.repository;

import com.example.userservice.entity.CohortMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CohortMemberRepository extends JpaRepository<CohortMemberEntity, Long> {
}
