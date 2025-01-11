package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity(name = "cohort_member")
public class CohortMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "cohort_id", nullable = false)
    CohortEntity cohort;

    @Column(name = "user_id")
    String keycloakId;

    @Column(name = "available")
    Short available;

    @Column(name = "added_time")
    Date addedTime;

}
