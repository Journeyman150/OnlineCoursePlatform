package com.example.controllers.api.user;

import com.example.domain.Course;
import com.example.domain.Lesson;
import com.example.exception_handling.api.AccessToCourseDeniedException;
import com.example.exception_handling.api.NoSuchCourseException;
import com.example.service.CourseService;
import com.example.service.CourseSubscribeService;
import com.example.service.LessonService;
import com.example.service.UserService;
import com.example.service.access.AccessControlService;
import com.example.service.access.CourseInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user/courses")
public class CoursesRESTUserController {
    @Value("${authorRolePrice}")
    private int authorRolePrice;

    private UserService userService;
    private CourseService courseService;
    private LessonService lessonService;
    private CourseSubscribeService courseSubscribeService;
    private AccessControlService accessControlService;
    private CourseInvitationService courseInvitationService;

    @Autowired
    public CoursesRESTUserController (UserService userService,
                          CourseService courseService,
                          LessonService lessonService,
                          CourseSubscribeService courseSubscribeService,
                          AccessControlService accessControlService,
                          CourseInvitationService courseInvitationService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.courseSubscribeService = courseSubscribeService;
        this.accessControlService = accessControlService;
        this.courseInvitationService = courseInvitationService;
    }

    @GetMapping()
    public List<Course> getCoursesListByKeyword(@RequestParam("keyword") String keyword) {
        List<Course> coursesList = courseService.findCourses(keyword);
        if (coursesList.isEmpty()) {
            throw new NoSuchCourseException("No courses by entered keyword(s) found.");
        }
        return coursesList;
    }

    @GetMapping("{courseId}")
    public Course getCourseById(@PathVariable("courseId") long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new NoSuchCourseException("Course with id " + courseId + " not found");
        }
        long userId = userService.getAuthorizedUser().getId();
        if (course.isNonPublic() && !accessControlService.userHasAccessToCourse(userId, courseId)) {
            throw new AccessToCourseDeniedException(
                    "User with id " + userId + " has no access to non public course with id " + courseId);
        }
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        return course;
    }
}
