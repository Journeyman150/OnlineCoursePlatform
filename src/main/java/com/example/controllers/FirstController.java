package com.example.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/first")
public class FirstController {
    @GetMapping("/hello")
    public String sayHello(HttpServletRequest request, Model model) {

        String name = request.getParameter("name");
        String surname = request.getParameter("surname");
        model.addAttribute("name", name);
        model.addAttribute("surname", surname);
        return "first/hello";
    }
    @GetMapping("/goodbye")
    public String sayGoodBye() {
        return "first/goodbye";
    }
}
