package com.example.courseservice.repository;

import com.example.courseservice.dto.StudentProgressDTO;
import com.example.courseservice.entity.LessonBranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonBranchRepository extends JpaRepository<LessonBranchEntity, Long> {

    @Query("select l from lesson_branch l where l.lessonPages.id = :lessonPagesId and l.userId = :userId")
    public LessonBranchEntity findLessonBranchEntityByLessonPagesIdAndUserId(Long lessonPagesId, String userId);

    @Query("select new com.example.courseservice.dto.StudentProgressDTO(lb.id, l.id, lp.id, lp.title, lb.userId, lt.completed, lt.startTime, SUM (lb.timeSeen)) " +
            "from lesson_branch lb left join lesson_pages lp " +
            "on lb.lessonPages.id = lp.id " +
            "left join lesson l " +
            "on lp.lesson.id = l.id " +
            "left join lesson_timer lt " +
            "on lt.lesson.id = l.id " +
            "where l.id = :lessonId and lb.userId = :userId")
    public List<StudentProgressDTO> getStudentProgressByLessonPagesIdAndUserId(@Param("lessonId") Long lessonId,
                                                                               @Param("userId") String userId);


    @Query("select new com.example.courseservice.dto.StudentProgressDTO(null , l.id, null , l.name, ue.userId, lt.completed, null , SUM (lb.timeSeen)) " +
            "from user_enrolment ue left join lesson_branch lb " +
            "on ue.userId = lb.userId " +
            "left join lesson_pages lp " +
            "on lb.lessonPages.id = lp.id " +
            "left join lesson l " +
            "on lp.lesson.id = l.id and l.id =:lessonId " +
            "left join lesson_timer lt " +
            "on lt.lesson.id = l.id " +
            "group by ue.userId, l.id, l.name ")
    public List<StudentProgressDTO> getStudentProgressByLessonId(Long lessonId);
}
