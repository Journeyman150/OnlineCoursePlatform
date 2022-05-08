package com.example.controllers;

import com.example.domain.Course;
import com.example.domain.Lesson;
import com.example.domain.User;
import com.example.search_engine.CoursesSearchData;
import com.example.service.CourseService;
import com.example.service.CourseSubscribingUserService;
import com.example.service.LessonService;
import com.example.service.UserService;
import com.example.service.access.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserController {
    private UserService userService;
    private CourseService courseService;
    private CoursesSearchData coursesSearchData;
    private LessonService lessonService;
    private CourseSubscribingUserService courseSubscribingUserService;
    private AccessControlService accessControlService;

    @Autowired
        public UserController(UserService userService,
                              CourseService courseService,
                              CoursesSearchData coursesSearchData,
                              LessonService lessonService,
                              CourseSubscribingUserService courseSubscribingUserService,
                              AccessControlService accessControlService) {
        this.userService = userService;
        this.courseService = courseService;
        this.coursesSearchData = coursesSearchData;
        this.lessonService = lessonService;
        this.courseSubscribingUserService = courseSubscribingUserService;
        this.accessControlService = accessControlService;
    }

    @GetMapping()
    public String getUserMainPage(@RequestParam(name = "keyword", required = false) String keyword,
                                  Model model) {
        if (keyword != null && !keyword.equals("") && !keyword.matches("\s+")) {
            List<Course> coursesList = new ArrayList<>();
            Set<Long> set = coursesSearchData.findIndexes(CoursesSearchData.getSeparateKeywords(keyword));
                System.out.println("In user controller: " + Arrays.toString(set.toArray()));
            if (set.contains(-1L)) {
                model.addAttribute("noResults", "The search has not given any results.");
                return "/user/main_page";
            }
            set.forEach(n -> coursesList.add(courseService.getCourseById(n)));
            model.addAttribute("coursesList", coursesList);
        }
        return "/user/main_page";
    }
    @GetMapping("/course/{id}")
    public String getCourse(@PathVariable("id") long courseId,
                            Model model) {
        Course course = courseService.getPublicCourseById(courseId);
        if (course == null) {
            return "/error_page";
        }
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        model.addAttribute("course", course);
        return "/user/course";
    }
    @PostMapping("/course/subscribe")
    public String subscribeToCourse(@RequestParam("courseId") long courseId,
                                    Model model) {
        Course course = courseService.getPublicCourseById(courseId);
        User user = userService.getAuthorizedUser();
        User author = userService.getUserById(course.getAuthorId());
        if (user.getBalance() < course.getPrice()) {
            model.addAttribute("transactionResults", "Not enough funds on your balance.");
        } else {
            courseSubscribingUserService.subscribeUserAndMakePayment(user, author, course);
            model.addAttribute("transactionResults", "You are subscribed to the course!");
        }
        model.addAttribute("course", course);
        return "/user/course";
    }
    @GetMapping("/course/{courseId}/lesson/{num}")
    public String getLesson(@PathVariable("courseId") long courseId,
                            @PathVariable("num") int num,
                            Model model) {
        User user = userService.getAuthorizedUser();
        if (!accessControlService.userHasAccessToCourse(user.getId(), courseId)) {
            model.addAttribute("errorMessage",
                    "You need subscribe to the course before watching the lessons");
            return "error_page";
        }
        Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, num);
        model.addAttribute("lesson", lesson);
        return "/user/lesson";
    }

    @GetMapping("/course/{courseId}/lesson/{num}/video")
    public ResponseEntity<Resource> getLessonVideo(@PathVariable long courseId,
                                                   @PathVariable int num) throws IOException {
        User user = userService.getAuthorizedUser();
        if (!accessControlService.userHasAccessToCourse(user.getId(), courseId)) {
            throw new AccessDeniedException("Access to file denied.");
        }
        Resource resource = new FileSystemResource(lessonService.getFile(courseId, num));
        return ResponseEntity.ok()
                .contentLength(10L*1024*1024*1024)
                .contentType(MediaType.MULTIPART_MIXED)
                .body(resource);
    }
}
