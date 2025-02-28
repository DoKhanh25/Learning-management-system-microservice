package com.example.courseservice.entity;


import com.example.courseservice.enums.ResourceDisplay;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "resource")
@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ResourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "name")
    String name;

    @Column(name = "intro")
    String intro;

    @Column(name = "display")
    ResourceDisplay display;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    CourseEntity course;


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "files_id", referencedColumnName = "id")
    FilesEntity files;



}
