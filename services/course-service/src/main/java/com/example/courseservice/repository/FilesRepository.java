package com.example.courseservice.repository;

import com.example.courseservice.entity.FilesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface FilesRepository extends JpaRepository<FilesEntity, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM files f WHERE f.filePath = :filePath")
    public void deleteFilesEntityByFilePath(@Param("filePath") String filePath);
}
