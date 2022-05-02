package com.example.controllers;

import com.example.domain.Course;
import com.example.domain.Lesson;
import com.example.domain.User;
import com.example.service.CourseService;
import com.example.service.LessonService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.*;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.util.List;

@Controller
@RequestMapping("/author")
public class AuthorController {
    private final UserService userService;
    private final CourseService courseService;
    private final LessonService lessonService;

    @Autowired
    public AuthorController(UserService userService, CourseService courseService, LessonService lessonService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
    }

    @GetMapping()
    public String getAuthorMainPage() {
        return "/author/main_page";
    }

    @GetMapping("/courses")
    public String getCoursesPage(Model model) {
        User author = userService.getAuthorizedUser();
        model.addAttribute("publicCoursesList", courseService.getPublicListByAuthorId(author.getId()));
        model.addAttribute("nonPublicCoursesList", courseService.getNonPublicListByAuthorId(author.getId()));
        return "/author/courses";
    }

    @GetMapping("/course/{id}")
    public String getCourse(@PathVariable long id, Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(id);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            course.setLessonsList(lessonService.getLessonsListByCourseId(id));
            model.addAttribute("course", course);
            return "/author/course";
        }
    }

    @GetMapping("/course/{courseId}/lesson/{num}")
    public String getLesson(@PathVariable long courseId,
                            @PathVariable int num,
                            Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, num);
            model.addAttribute("lesson", lesson);
            return "/author/lesson";
        }
    }
    @GetMapping("/course/{courseId}/lesson/{num}/video")
    public ResponseEntity<Resource> getLessonVideo(@PathVariable long courseId,
                                                   @PathVariable int num) throws IOException {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || author.getId() != course.getAuthorId()) {
            throw new AccessDeniedException("Access to file denied.");
        }
        return ResponseEntity.ok(new ByteArrayResource(Files.readAllBytes(lessonService.getFile(courseId, num).toPath())));
    }

    @GetMapping("/course/{courseId}/lessons/new")
    public String getNewLesson(@PathVariable long courseId, Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            Lesson lesson = new Lesson();
            lesson.setCourseId(course.getId());
            model.addAttribute("lesson", lesson);
            return "/author/lessons_new";
        }
    }

    @PostMapping("/course/{courseId}/lessons/new")
    public String createLesson(@PathVariable long courseId,
                               @ModelAttribute("lesson") @Valid Lesson lesson, BindingResult bindingResult,
                               @RequestParam(value = "videoFile", required = true) MultipartFile videoFile,
                               Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            if (bindingResult.hasErrors()) {
                return "/author/lessons_new";
            } else if (lessonService.isLessonNumExistInCourse(lesson.getNum(), course.getId())) {
                model.addAttribute("lessonNumError",
                        "Lesson with the given number already exists in this course.");
                return "/author/lessons_new";
            }
            lessonService.save(lesson, courseId, videoFile);
            return "redirect:/author/course/" + courseId;
        }
    }

    @GetMapping("/course/{courseId}/lesson/{num}/edit")
    public String getLessonEdit(@PathVariable long courseId,
                            @PathVariable int num,
                            Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, num);
            model.addAttribute("lesson", lesson);
            return "/author/lesson_edit";
        }
    }

    @PatchMapping("/course/{courseId}/lesson/{num}")
    public String editLesson(@PathVariable long courseId,
                               @PathVariable int num,
                               @ModelAttribute("lesson") @Valid Lesson lesson, BindingResult bindingResult,
                               @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                               Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            if (bindingResult.hasErrors()) {
                return "/author/lesson_edit";
            } else if (lesson.getNum() != num && lessonService.isLessonNumExistInCourse(lesson.getNum(), course.getId())) {
                model.addAttribute("lessonNumError",
                        "Lesson with the given number already exists in this course.");
                return "/author/lesson_edit";
            }
            lessonService.update(lesson, courseId, num, videoFile);
            return "redirect:/author/course/" + courseId;
        }
    }

    @GetMapping("/courses/new")
    public String getCreatingCoursePage(Model model) {
        model.addAttribute("course", new Course());
        return "/author/courses_new";
    }

    @PostMapping("/courses/new")
    public String createCourse(@ModelAttribute("course") @Valid Course course, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "/author/courses_new";
        }
        User author = userService.getAuthorizedUser();
        courseService.save(course, author);
        return "redirect:/author/courses";
    }
}
