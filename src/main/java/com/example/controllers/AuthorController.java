package com.example.controllers;

import com.example.dao.CourseInvitationDAO;
import com.example.domain.Course;
import com.example.domain.Lesson;
import com.example.domain.User;
import com.example.service.CourseService;
import com.example.service.CourseSubscribeService;
import com.example.service.LessonService;
import com.example.service.UserService;
import com.example.service.access.AccessControlService;
import com.example.service.access.CourseInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/author")
public class AuthorController {
    @Value("${maxIconSize}")
    private long maxIconSize;
    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;
    private final AccessControlService accessControlService;
    private final CourseInvitationService courseInvitationService;
    private final CourseSubscribeService courseSubscribeService;

    @Autowired
    public AuthorController(UserService userService,
                            CourseService courseService,
                            LessonService lessonService,
                            AccessControlService accessControlService,
                            CourseInvitationService courseInvitationService,
                            CourseSubscribeService courseSubscribeService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.accessControlService = accessControlService;
        this.courseInvitationService = courseInvitationService;
        this.courseSubscribeService = courseSubscribeService;
    }
//
//    @GetMapping()
//    public String getAuthorMainPage(Model model) {
//        int balance = userService.getUserById(userService.getAuthorizedUser().getId()).getBalance();
//        model.addAttribute("balance", balance);
//        return "author/main_page";
//    }

    @GetMapping("/courses")
    public String getAuthorCoursesPage(Model model) {
        User author = userService.getUserById(userService.getAuthorizedUser().getId());

        List<Course> publicCoursesList = courseService.getPublicListByAuthorId(author.getId());
        List<Course> nonPublicCoursesList = courseService.getNonPublicListByAuthorId(author.getId());
        Map<Long, Long> subsMap = new HashMap<>();
        publicCoursesList.forEach(n ->
                subsMap.put(n.getId(), courseSubscribeService.getSubscribedUsersNumberByCourseId(n.getId())));
        nonPublicCoursesList.forEach(n ->
                subsMap.put(n.getId(), courseSubscribeService.getSubscribedUsersNumberByCourseId(n.getId())));

        model.addAttribute("subsMap", subsMap);
        model.addAttribute("publicCoursesList", publicCoursesList);
        model.addAttribute("nonPublicCoursesList", nonPublicCoursesList);
        model.addAttribute("balance", author.getBalance());
        return "author/courses";
    }

    @GetMapping("/course/{courseId}")
    public String getCourse(@PathVariable("courseId") long courseId, Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        Course course = courseService.getCourseById(courseId);
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        model.addAttribute("course", course);
        model.addAttribute("subscribersNum", courseSubscribeService.getSubscribedUsersNumberByCourseId(courseId));
        return "author/course";
    }

    @PostMapping("/course/{courseId}/invite")
    public String inviteUser(@PathVariable("courseId") long courseId,
                             @RequestParam("userEmail") String userEmail,
                             Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        Long userId = userService.findUserId(userEmail);
        if (userId == null) {
            model.addAttribute("message", "User with entered email not found.");
            return getCourse(courseId, model);
        }
        if (courseInvitationService.getInvitedUsersIdListByCourseId(courseId).contains(userId)) {
            model.addAttribute("message", "User with entered email already invited.");
            return getCourse(courseId, model);
        }
        courseInvitationService.addInvitation(courseId, userId);
        model.addAttribute("message", "User with email " + userEmail + " invited for this course.");
        return getCourse(courseId, model);
    }

    @GetMapping("/courses/new")
    public String getCreatingCoursePage(Model model) {
        model.addAttribute("course", new Course());
        return "author/courses_new";
    }

    @PostMapping("/courses/new")
    public String createCourse(@ModelAttribute("course") @Valid Course course, BindingResult bindingResult,
                               @RequestParam(value = "courseIcon") MultipartFile courseIcon,
                               Model model) {
        if (bindingResult.hasErrors()) {
            return "author/courses_new";
        }
        if (courseIcon.getSize() > maxIconSize) {
            model.addAttribute("iconSizeError", "Max icon size must be less than 1Mb.");
            return "author/courses_new";
        }
        User author = userService.getAuthorizedUser();
        course.setAuthorId(author.getId());
        courseService.save(course, courseIcon);
        return "redirect:/author/courses";
    }

    @GetMapping("/course/{id}/edit")
    public String getEditCoursePage(@PathVariable("id") long courseId,
                                    Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        model.addAttribute("course", courseService.getCourseById(courseId));
        return "author/course_edit";
    }

    @PatchMapping("/course/{id}")
    public String updateCourse(@PathVariable("id") long courseId,
                               @ModelAttribute("course") @Valid Course course, BindingResult bindingResult,
                               @RequestParam(value = "courseIcon") MultipartFile courseIcon,
                               Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        if (bindingResult.hasErrors()) {
            return "author/course_edit";
        }
        if (courseIcon.getSize() > maxIconSize) {
            model.addAttribute("iconSizeError", "Max icon size must be less than 1Mb.");
            return "author/course_edit";
        }
        courseService.update(course, courseIcon, courseId);
        return "redirect:/author/courses";
    }

    @GetMapping("/course/{courseId}/lesson/{num}")
    public String getLesson(@PathVariable long courseId,
                            @PathVariable int num,
                            Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, num);
        if (lesson == null) {
            model.addAttribute("errorMessage", "Lesson doesn't exist.");
            return "error/error_page";
        }
        Course course = courseService.getCourseById(courseId);
        model.addAttribute("lesson", lesson);
        model.addAttribute("course", course);
        return "author/lesson";
    }

    @GetMapping("/course/{courseId}/lesson/{num}/video")
    public ResponseEntity<Resource> getLessonVideo(@PathVariable long courseId,
                                                   @PathVariable int num) throws IOException {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            throw new AccessDeniedException("Access to file denied.");
        }
        Resource resource = new FileSystemResource(lessonService.getFile(courseId, num));
        return ResponseEntity.ok()
                .contentLength(10L*1024*1024*1024)
                .contentType(MediaType.MULTIPART_MIXED)
                .body(resource);
    }

    @GetMapping("/course/{courseId}/lessons/new")
    public String getNewLesson(@PathVariable long courseId, Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        Lesson lesson = new Lesson();
        lesson.setCourseId(courseId);
        model.addAttribute("lesson", lesson);
        return "author/lessons_new";
    }

    @PostMapping("/course/{courseId}/lessons/new")
    public String createLesson(@PathVariable long courseId,
                               @ModelAttribute("lesson") @Valid Lesson lesson, BindingResult bindingResult,
                               @RequestParam(value = "videoFile") MultipartFile videoFile,
                               Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        if (bindingResult.hasErrors()) {
            return "author/lessons_new";
        } else if (lessonService.isLessonNumExistInCourse(lesson.getNum(), courseId)) {
            model.addAttribute("lessonNumError",
                    "Lesson with the given number already exists in this course.");
            return "author/lessons_new";
        }
        lessonService.save(lesson, courseId, videoFile);
        return "redirect:/author/course/" + courseId;
    }

    @GetMapping("/course/{courseId}/lesson/{num}/edit")
    public String getLessonEdit(@PathVariable long courseId,
                                @PathVariable int num,
                                Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, num);
        model.addAttribute("lesson", lesson);
        return "author/lesson_edit";
    }

    @PatchMapping("/course/{courseId}/lesson/{num}")
    public String editLesson(@PathVariable long courseId,
                               @PathVariable int num,
                               @ModelAttribute("lesson") @Valid Lesson lesson, BindingResult bindingResult,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        if (bindingResult.hasErrors()) {
            return "author/lesson_edit";
        } else if (lesson.getNum() != num && lessonService.isLessonNumExistInCourse(lesson.getNum(), courseId)) {
            model.addAttribute("lessonNumError",
                    "Lesson with the given number already exists in this course.");
            return "author/lesson_edit";
        }
        lessonService.update(lesson, courseId, num, videoFile);
        return "redirect:/author/course/" + courseId;
    }

    @DeleteMapping("/course/{courseId}/lesson/{num}")
    public String deleteLesson(@PathVariable("courseId") long courseId,
                               @PathVariable("num") int num,
                               Model model) {
        User author = userService.getAuthorizedUser();
        if (!accessControlService.authorHasAccessToCourse(author, courseId)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        lessonService.delete(courseId, num);
        return "redirect:/author/course/" + courseId;
    }
}
