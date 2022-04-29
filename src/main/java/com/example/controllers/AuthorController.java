package com.example.controllers;

import com.example.domain.Course;
import com.example.domain.User;
import com.example.service.CourseService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping("/author")
public class AuthorController {
    private final UserService userService;
    private final CourseService courseService;

    @Autowired
    public AuthorController(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
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
    public String getCourseById(@PathVariable long id, Model model) {
        User author = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(id);
        if (course == null || author.getId() != course.getAuthorId()) {
            return "/error_page";
        } else {
            model.addAttribute("course", course);
            return "/author/course";
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
