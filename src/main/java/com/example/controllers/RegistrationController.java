package com.example.controllers;

import com.example.dao.UserDAO;
import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Controller
public class RegistrationController {
    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }
    @PostMapping("/registration")
    public String createUser(@ModelAttribute("user") @Valid User user,
                          BindingResult bindingResult,
                          @RequestParam(name = "confirmPassword") String confirmPassword,
                          Model model) {
        user.setEmail(user.getEmail().toLowerCase());
        if (bindingResult.hasErrors()) {
            return "registration";
        }
        if (userService.isUserAlreadyExist(user.getEmail())) {
            model.addAttribute("userAlreadyExistMessage",
                    "Пользователь с таким адресом электронной почты уже существует.");
            return "registration";
        }
        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("confirmPasswordErrorMessage", "Пароли не совпадают.");
            return "registration";
        }
        userService.save(user);
        return "login";
    }
}
