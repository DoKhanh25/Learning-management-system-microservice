package com.example.courseservice.dto;


import com.example.courseservice.enums.ResourceDisplay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {
    String name;
    String intro;
    ResourceDisplay display;
    CourseDTO course;
    FilesDTO files;

}
