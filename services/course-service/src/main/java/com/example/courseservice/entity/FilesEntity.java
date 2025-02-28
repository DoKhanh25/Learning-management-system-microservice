package com.example.courseservice.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;

@Entity(name = "files")

@Data
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class FilesEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @OneToOne(mappedBy = "files", fetch = FetchType.LAZY)
    private ResourceEntity resource;

    @Column(name = "file_path")
    String filePath;

    @Column(name = "file_name")
    String fileName;

    @Column(name = "file_size")
    String fileSize;

    @Column(name = "author")
    String author;
}
