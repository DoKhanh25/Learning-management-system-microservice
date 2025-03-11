package com.example.courseservice.dto;

import com.example.courseservice.entity.ResourceEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilesDTO {
    Long id;
    ResourceEntity resource;
    String filePath;
    String fileName;
    String fileSize;
    String author;
}
