package com.example.controllers;

import com.example.configs.WebSecurityConfig;
import com.example.dao.UserDAO;
import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;

@Controller
public class RegistrationController {
    private final UserDAO userDAO;
    private final UserService userService;

    @Autowired
    public RegistrationController(UserDAO userDAO, UserService userService) {
        this.userDAO = userDAO;
        this.userService = userService;
    }

    @GetMapping("/registration")
    public String registration(Model model) {
        model.addAttribute("user", new User());
        return "/user/registration";
    }
    @PostMapping("/registration")
    public String addUser(@ModelAttribute("user") @Valid User user, BindingResult bindingResult, Model model) {
        user.setEmail(user.getEmail().toLowerCase());
        if (bindingResult.hasErrors()) {
            return "/user/registration";
        }
        if (userService.userAlreadyExist(user.getEmail())) {
            model.addAttribute("userAlreadyExistMessage",
                    "Пользователь с таким адресом электронной почты уже существует.");
            return "/user/registration";
        }
        if (!user.getConfirmPassword().equals(user.getPassword())) {
            model.addAttribute("confirmPasswordErrorMessage", "Пароли не совпадают.");
            return "/user/registration";
        }
        user.setPassword(userService.getEncodedPassword(user.getPassword()));
        userDAO.addUser(user);
        return "/user/login";
    }
}
