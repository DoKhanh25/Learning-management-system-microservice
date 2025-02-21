package com.example.userservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity(name = "cohort")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class CohortEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;


    @Column(name = "name")
    String name;

    @Column(name = "description")
    String description;


    @OneToMany(mappedBy = "cohort", cascade = CascadeType.ALL, orphanRemoval = true)
    List<CohortMemberEntity> cohortMembers;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "available")
    Short available;

    @Column(name = "updated_time")
    Date updatedTime;


}
