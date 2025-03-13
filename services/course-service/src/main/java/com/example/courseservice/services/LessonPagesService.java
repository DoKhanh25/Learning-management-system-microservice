package com.example.courseservice.services;

import com.example.courseservice.context.CycleAvoidingMappingContext;
import com.example.courseservice.dto.LessonPagesDTO;
import com.example.courseservice.dto.ResultDTO;
import com.example.courseservice.entity.FilesEntity;
import com.example.courseservice.entity.LessonEntity;
import com.example.courseservice.entity.LessonPagesEntity;
import com.example.courseservice.entity.ResourceEntity;
import com.example.courseservice.enums.QType;
import com.example.courseservice.mapper.LessonPagesMapper;
import com.example.courseservice.repository.CourseRepository;
import com.example.courseservice.repository.FilesRepository;
import com.example.courseservice.repository.LessonPagesRepository;
import com.example.courseservice.repository.LessonRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
public class LessonPagesService {
    @Autowired
    LessonPagesRepository lessonPagesRepository;

    @Autowired
    LessonRepository lessonRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    FilesRepository filesRepository;

    @Autowired
    LessonPagesMapper lessonPagesMapper;

    public ResponseEntity<ResultDTO> addContentLessonPage(LessonPagesDTO lessonPagesDTO){
        ResultDTO resultDTO = new ResultDTO();
        if (lessonPagesDTO == null || lessonPagesDTO.getTitle() == null || lessonPagesDTO.getTitle().isBlank() || lessonPagesDTO.getQType() == null) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Invalid data");
            return ResponseEntity.badRequest().body(resultDTO);
        }
        if (lessonPagesDTO.getLessonId() == null || lessonRepository.findById(lessonPagesDTO.getLessonId()).isEmpty()) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Lesson not found");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        if(!QType.CONTENT.toString().equals(lessonPagesDTO.getQType())){
            resultDTO.setStatus(2);
            resultDTO.setMessage("Invalid data");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        LessonPagesEntity lessonPagesEntity = lessonPagesMapper.toEntity(lessonPagesDTO, new CycleAvoidingMappingContext());
        lessonPagesEntity.setLesson(lessonRepository.findById(lessonPagesDTO.getLessonId()).get());
        lessonPagesEntity.setCreatedTime(new Date());

        lessonPagesEntity = lessonPagesRepository.save(lessonPagesEntity);
        resultDTO.setData(lessonPagesMapper.toDto(lessonPagesEntity, new CycleAvoidingMappingContext()));
        resultDTO.setStatus(1);
        return ResponseEntity.ok(resultDTO);
    }
    @Transactional
    public ResponseEntity<ResultDTO> addDocumentLessonPage(LessonPagesDTO lessonPagesDTO, MultipartFile file){
        ResultDTO resultDTO = new ResultDTO();
        log.info("addDocumentLessonPage: {}", lessonPagesDTO.toString());
        try {
            // Input validation
            if (lessonPagesDTO == null || lessonPagesDTO.getTitle() == null || lessonPagesDTO.getTitle().isBlank() || lessonPagesDTO.getQType() == null) {
                resultDTO.setStatus(2);
                resultDTO.setMessage("Invalid data");
                return ResponseEntity.badRequest().body(resultDTO);
            }
            Optional<LessonEntity> lessonEntityOptional = lessonRepository.findById(lessonPagesDTO.getLessonId());

            if (lessonPagesDTO.getLessonId() == null || lessonEntityOptional.isEmpty()) {
                resultDTO.setStatus(2);
                resultDTO.setMessage("Lesson not found");
                return ResponseEntity.badRequest().body(resultDTO);
            }

            if(!QType.DOCUMENT.toString().equals(lessonPagesDTO.getQType())){
                resultDTO.setStatus(2);
                resultDTO.setMessage("Invalid data");
                return ResponseEntity.badRequest().body(resultDTO);
            }

            // Validate file
            if (file == null || file.isEmpty()) {
                resultDTO.setStatus(2);
                resultDTO.setMessage("Document file is required");
                return ResponseEntity.badRequest().body(resultDTO);
            }

            // Create FilesEntity to store file information
            FilesEntity filesEntity = new FilesEntity();
            filesEntity.setFileName(file.getOriginalFilename());
            filesEntity.setFileSize(String.valueOf(file.getSize()));

            // Save file to disk
            String uploadDir = "uploads/documents";
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = saveFile(uploadDir, fileName, file);

            filesEntity.setFilePath(filePath);
            filesEntity = filesRepository.save(filesEntity);

            LessonPagesEntity lessonPagesEntity = lessonPagesMapper.toEntity(lessonPagesDTO, new CycleAvoidingMappingContext());
            lessonPagesEntity.setTitle(lessonPagesDTO.getTitle());
            lessonPagesEntity.setPosition(lessonPagesDTO.getPosition());
            lessonPagesEntity.setLesson(lessonEntityOptional.get());
            lessonPagesEntity.setCreatedTime(new Date());
            lessonPagesEntity.setQType(QType.DOCUMENT); // Explicitly set as DOCUMENT
            lessonPagesEntity.setContent(filePath); // Store file path in content field

            lessonPagesEntity = lessonPagesRepository.save(lessonPagesEntity);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Document lesson page added successfully");
            resultDTO.setData(lessonPagesMapper.toDto(lessonPagesEntity, new CycleAvoidingMappingContext()));
            return ResponseEntity.ok(resultDTO);

        } catch (Exception e) {
            log.error("Error adding document lesson page: {}", e.getMessage(), e);
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to add document lesson page: " + e.getMessage());
            return ResponseEntity.ok(resultDTO);
        }
    }

    @Transactional
    public ResponseEntity<ResultDTO> addUploadVideoLessonPage(LessonPagesDTO lessonPagesDTO, MultipartFile file){
        ResultDTO resultDTO = new ResultDTO();
        try {
            // Input validation
            if (lessonPagesDTO == null || lessonPagesDTO.getTitle() == null || lessonPagesDTO.getTitle().isBlank() || lessonPagesDTO.getQType() == null) {
                resultDTO.setStatus(2);
                resultDTO.setMessage("Invalid data");
                return ResponseEntity.badRequest().body(resultDTO);
            }
            Optional<LessonEntity> lessonEntityOptional = lessonRepository.findById(lessonPagesDTO.getLessonId());

            if (lessonPagesDTO.getLessonId() == null || lessonEntityOptional.isEmpty()) {
                resultDTO.setStatus(2);
                resultDTO.setMessage("Lesson not found");
                return ResponseEntity.badRequest().body(resultDTO);
            }

            if(!QType.VIDEO.toString().equals(lessonPagesDTO.getQType())){
                resultDTO.setStatus(2);
                resultDTO.setMessage("Invalid data");
                return ResponseEntity.badRequest().body(resultDTO);
            }

            if(lessonPagesDTO.getContent().contains("http") || lessonPagesDTO.getContent().contains("https")){
                LessonPagesEntity lessonPagesEntity = lessonPagesMapper.toEntity(lessonPagesDTO, new CycleAvoidingMappingContext());
                lessonPagesEntity.setLesson(lessonEntityOptional.get());
                lessonPagesEntity.setCreatedTime(new Date());
                LessonPagesEntity lessonPagesEntityResult = lessonPagesRepository.save(lessonPagesEntity);

                resultDTO.setStatus(1);
                resultDTO.setMessage("Video lesson page added successfully");
                resultDTO.setData(lessonPagesMapper.toDto(lessonPagesEntityResult, new CycleAvoidingMappingContext()));
                return ResponseEntity.ok(resultDTO);
            }

            // Validate file
            if (file == null || file.isEmpty()) {
                resultDTO.setStatus(2);
                resultDTO.setMessage("Document file is required");
                return ResponseEntity.badRequest().body(resultDTO);
            }

            FilesEntity filesEntity = new FilesEntity();
            filesEntity.setFileName(file.getOriginalFilename());
            filesEntity.setFileSize(String.valueOf(file.getSize()));

            // Save file to disk
            String uploadDir = "uploads/videos";
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String filePath = saveFile(uploadDir, fileName, file);

            filesEntity.setFilePath(filePath);
            filesEntity = filesRepository.save(filesEntity);

            LessonPagesEntity lessonPagesEntity = lessonPagesMapper.toEntity(lessonPagesDTO, new CycleAvoidingMappingContext());
            lessonPagesEntity.setTitle(lessonPagesDTO.getTitle());
            lessonPagesEntity.setPosition(lessonPagesDTO.getPosition());
            lessonPagesEntity.setLesson(lessonEntityOptional.get());
            lessonPagesEntity.setCreatedTime(new Date());
            lessonPagesEntity.setQType(QType.VIDEO); // Explicitly set as DOCUMENT
            lessonPagesEntity.setContent(filePath); // Store file path in content field

            lessonPagesEntity = lessonPagesRepository.save(lessonPagesEntity);

            resultDTO.setStatus(1);
            resultDTO.setMessage("Document lesson page added successfully");
            resultDTO.setData(lessonPagesMapper.toDto(lessonPagesEntity, new CycleAvoidingMappingContext()));
            return ResponseEntity.ok(resultDTO);

        } catch (Exception e) {
            log.error("Error adding video lesson page: {}", e.getMessage(), e);
            resultDTO.setStatus(0);
            resultDTO.setMessage("Failed to add video lesson page: " + e.getMessage());
            return ResponseEntity.ok(resultDTO);
        }
    }

    public ResponseEntity<ResultDTO> addVideoURLLessonPage(LessonPagesDTO lessonPagesDTO){
        ResultDTO resultDTO = new ResultDTO();
        Optional<LessonEntity> lessonEntityOptional = lessonRepository.findById(lessonPagesDTO.getLessonId());
        if (lessonPagesDTO == null || lessonPagesDTO.getTitle() == null || lessonPagesDTO.getTitle().isBlank() || lessonPagesDTO.getQType() == null) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Invalid data");
            return ResponseEntity.badRequest().body(resultDTO);
        }
        if (lessonPagesDTO.getLessonId() == null || lessonEntityOptional.isEmpty()) {
            resultDTO.setStatus(2);
            resultDTO.setMessage("Lesson not found");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        if(!(lessonPagesDTO.getContent().contains("http") || lessonPagesDTO.getContent().contains("https"))){
            resultDTO.setStatus(2);
            resultDTO.setMessage("Invalid data");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        if(!QType.VIDEO.toString().equals(lessonPagesDTO.getQType())){
            resultDTO.setStatus(2);
            resultDTO.setMessage("Invalid data");
            return ResponseEntity.badRequest().body(resultDTO);
        }

        LessonPagesEntity lessonPagesEntity = lessonPagesMapper.toEntity(lessonPagesDTO, new CycleAvoidingMappingContext());
        lessonPagesEntity.setLesson(lessonEntityOptional.get());
        lessonPagesEntity.setCreatedTime(new Date());
        lessonPagesEntity.setQType(QType.VIDEO);
        LessonPagesEntity result = lessonPagesRepository.save(lessonPagesEntity);

        resultDTO.setStatus(1);
        resultDTO.setMessage("Video lesson page added successfully");
        resultDTO.setData(lessonPagesMapper.toDto(result, new CycleAvoidingMappingContext()));
        return ResponseEntity.ok(resultDTO);

    }

    @Transactional
    public ResponseEntity<ResultDTO> deleteLessonPageById(Long id){
        ResultDTO resultDTO = new ResultDTO();
        LessonPagesEntity lessonPagesEntity = lessonPagesRepository.findById(id).orElse(null);

        if(lessonPagesEntity == null){
            resultDTO.setStatus(2);
            resultDTO.setMessage("No data");
            return ResponseEntity.ok(resultDTO);
        }

        QType qType = lessonPagesEntity.getQType();
        switch (qType){
            case CONTENT:
                lessonPagesRepository.deleteById(id);
                resultDTO.setStatus(1);
                resultDTO.setMessage("Success");
                return ResponseEntity.ok(resultDTO);

            case DOCUMENT:
                try {
                    Path fileToDelete = Paths.get(lessonPagesEntity.getContent());
                    Files.deleteIfExists(fileToDelete);

                    filesRepository.deleteFilesEntityByFilePath(lessonPagesEntity.getContent());
                    lessonPagesRepository.deleteById(id);
                    resultDTO.setStatus(1);
                    resultDTO.setMessage("Success");
                    return ResponseEntity.ok(resultDTO);

                } catch (IOException e) {
                    log.error("Failed to delete file: {}", lessonPagesEntity.getContent(), e);
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("Failed to delete file: " + e.getMessage());
                    return ResponseEntity.ok(resultDTO);
                }
            case VIDEO:
                try {
                    if(lessonPagesEntity.getContent().contains("uploads/videos")){
                        Path fileToDelete = Paths.get(lessonPagesEntity.getContent());
                        Files.deleteIfExists(fileToDelete);

                        filesRepository.deleteFilesEntityByFilePath(lessonPagesEntity.getContent());
                        lessonPagesRepository.deleteById(id);

                        resultDTO.setStatus(1);
                        resultDTO.setMessage("Success");
                        return ResponseEntity.ok(resultDTO);
                    } else {

                        lessonPagesRepository.deleteById(id);
                        resultDTO.setStatus(1);
                        resultDTO.setMessage("Success");
                        return ResponseEntity.ok(resultDTO);
                    }

                } catch (IOException e) {
                    log.error("Failed to delete file: {}", lessonPagesEntity.getContent(), e);
                    resultDTO.setStatus(0);
                    resultDTO.setMessage("Failed to delete file: " + e.getMessage());
                    return ResponseEntity.ok(resultDTO);
                }



            default:
                resultDTO.setStatus(2);
                resultDTO.setMessage("Invalid data");
                return ResponseEntity.ok(resultDTO);
        }
    }


    private String saveFile(String uploadDir, String fileName, MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + "/" + fileName;
    }
}
