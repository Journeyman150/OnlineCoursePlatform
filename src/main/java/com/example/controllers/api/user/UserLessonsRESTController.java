package com.example.controllers.api.user;

import com.example.domain.Lesson;
import com.example.exception_handling.api.AccessToLessonDeniedException;
import com.example.service.LessonService;
import com.example.service.UserService;
import com.example.service.access.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/lessons")
public class UserLessonsRESTController {

    private UserService userService;
    private LessonService lessonService;
    private AccessControlService accessControlService;

    @Autowired
    public UserLessonsRESTController(UserService userService,
                                     LessonService lessonService,
                                     AccessControlService accessControlService) {
        this.userService = userService;
        this.lessonService = lessonService;
        this.accessControlService = accessControlService;
    }

    @GetMapping("/{courseId}/{lessonNum}")
    public Lesson getLessonById(@PathVariable("courseId") long courseId,
                                @PathVariable("lessonNum") int lessonNum) {
        long userId = userService.getAuthorizedUser().getId();
        Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, lessonNum);
        if (lesson == null
                || !accessControlService.userHasAccessToCourse(userId, lesson.getCourseId())) {
            throw new AccessToLessonDeniedException(
                    "User with id " + userId + " has no access to lessons of the course with id " + courseId);
        }
        return lesson;
    }

    @GetMapping("/{lessonId}")
    public Lesson getLessonById(@PathVariable("lessonId") long lessonId) {
        long userId = userService.getAuthorizedUser().getId();
        Lesson lesson = lessonService.getLessonById(lessonId);
        if (lesson == null
                || !accessControlService.userHasAccessToCourse(userId, lesson.getCourseId())) {
            throw new AccessToLessonDeniedException(
                    "User with id " + userId + " has no access to lesson with id " + lessonId);
        }
        return lesson;
    }

}
