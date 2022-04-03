package com.example.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/author")
public class AuthorController {

    @GetMapping()
    public String getAuthorMainPage() {
        return "/author/main_page";
    }

    @GetMapping("/account")
    public String getAccountPage() {
        return "redirect:/account";
    }

    @GetMapping("/courses")
    public String getCoursesPage(Model model) {
        //model.addAttribute("coursesList", );
        return "/author/courses";
    }
}
