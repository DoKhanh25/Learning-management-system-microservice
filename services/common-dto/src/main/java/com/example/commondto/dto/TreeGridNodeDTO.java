package com.example.commondto.dto;

import lombok.Data;

@Data
public class TreeGridNodeDTO {
    private Long id;
    private String title;
    private Long parentId;
    private String type;
}

