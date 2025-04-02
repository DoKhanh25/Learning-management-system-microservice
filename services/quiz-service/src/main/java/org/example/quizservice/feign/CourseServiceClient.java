package org.example.quizservice.feign;


import com.example.commondto.dto.ResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "course-service")
public interface CourseServiceClient {
    @GetMapping("/validateQuiz/validateIsTeacherInCourse")
    ResultDTO validateIsTeacherInCourse(@RequestParam("courseId") Long courseId,
                                        @RequestParam("userId") String userId);

    @GetMapping("/validateQuiz/validateIsInCourse")
    public ResultDTO validateIsCourse(@RequestParam("courseId") Long courseId,
                                      @RequestParam("userId") String userId);


}
