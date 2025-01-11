package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity(name = "user_picture")
@Data
public class UserPictureEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @Column(name = "id_map_keycloak")
    String keyCloakId;

    @Column(name = "picture_url")
    String pictureUrl;

    @Column(name = "created_time")
    Date createdTime;

    @Column(name = "updated_time")
    Date updatedTime;

}
