package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity(name = "cohort")
@Data
public class CohortEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "context_id")
    Long contextId;

    @Column(name = "name")
    String name;

    @Column(name = "description")
    String description;

    @Column(name = "id_number")
    Long idNumber;

    @OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CohortMemberEntity> cohortMembers;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "available")
    Short available;

    @Column(name = "updated_time")
    Date updatedTime;


}
