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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/user")
public class UserController {
    private UserService userService;
    private CourseService courseService;
    private LessonService lessonService;
    private CourseSubscribingUserService courseSubscribingUserService;
    private AccessControlService accessControlService;

    @Autowired
    public UserController(UserService userService,
                          CourseService courseService,
                          LessonService lessonService,
                          CourseSubscribingUserService courseSubscribingUserService,
                          AccessControlService accessControlService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.courseSubscribingUserService = courseSubscribingUserService;
        this.accessControlService = accessControlService;
    }

//    @GetMapping()
//    public String getUserMainPage(@RequestParam(name = "keyword", required = false) String keyword,
//                                  Model model) {
//        if (keyword != null && !keyword.equals("") && !keyword.matches("\s+")) {
//            List<Course> coursesList = new ArrayList<>();
//            Set<Long> set = coursesSearchData.findIndexes(CoursesSearchData.getSeparateKeywords(keyword));
//                System.out.println("In user controller: " + Arrays.toString(set.toArray()));
//            if (set.contains(-1L)) {
//                model.addAttribute("noResults", "The search has not given any results.");
//                return "/user/main_page";
//            }
//            set.forEach(n -> coursesList.add(courseService.getCourseById(n)));
//            model.addAttribute("coursesList", coursesList);
//        }
//        return "/user/main_page";
//    }
    @GetMapping()
    public String getUserMainPage(Model model,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(2);
        Page<Course> coursesPage = courseService.findPaginated(keyword, PageRequest.of(currentPage - 1, pageSize));
        int totalPages = coursesPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        Map<Long, String> authorMap = new HashMap<>();
        for (int i = 0; i < coursesPage.getContent().size(); i++) {
            long authorId = coursesPage.getContent().get(i).getAuthorId();
            String authorName = userService.getUserById(authorId).getName() + " " + userService.getUserById(authorId).getSurname();
            authorMap.put(authorId, authorName);
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("authorMap", authorMap);
        if (coursesPage.isEmpty() && keyword != null && !keyword.equals("")&& !keyword.matches("\s+")) {
            model.addAttribute("noResults", "The search has not given any results.");
        }
        return "/user/main_page";
    }
    @GetMapping("/course/{id}")
    public String getCourse(@PathVariable("id") long courseId,
                            Model model) {
        User user = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        boolean userHasAccessToCourse = accessControlService.userHasAccessToCourse(user.getId(), courseId);
        if (course == null || (course.isNonPublic() & userHasAccessToCourse)) {
            return "/error_page";
        }
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        model.addAttribute("course", course);
        model.addAttribute("userHasAccessToCourse", userHasAccessToCourse);
        return "/user/course";
    }
    @PostMapping("/course/subscribe")
    public String subscribeToCourse(@RequestParam("courseId") long courseId,
                                    Model model) {
        Course course = courseService.getPublicCourseById(courseId);
        User user = userService.getAuthorizedUser();
        if (course == null || accessControlService.userHasAccessToCourse(user.getId(), course.getId())) {
            return "/error_page";
        }
        User author = userService.getUserById(course.getAuthorId());
        if (user.getBalance() < course.getPrice()) {
            model.addAttribute("transactionResults", "Not enough funds on your balance.");
        } else {
            courseSubscribingUserService.subscribeUserAndMakePayment(user, author, course);
        }
        boolean userHasAccessToCourse = accessControlService.userHasAccessToCourse(user.getId(), courseId);
        model.addAttribute("course", course);
        model.addAttribute("userHasAccessToCourse", userHasAccessToCourse);
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
