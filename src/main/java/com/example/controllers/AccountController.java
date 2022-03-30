package com.example.controllers;

import com.example.configs.WebSecurityConfig;
import com.example.dao.UserDAO;
import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {
    private final UserDAO userDAO;
    private final UserService userService;

    @Autowired
    public AccountController(UserDAO userDAO, UserService userService) {
        this.userDAO = userDAO;
        this.userService = userService;
    }

    @GetMapping("/account")
    public String getAccountPage(Model model) {
        model.addAttribute("user", userService.getAuthorizedUser());
        return "/user/account";
    }

    @PostMapping("/account/change_password")
    public String changePassword(@ModelAttribute("user") User user,
                                 @RequestParam(name = "currentPassword") String currentPassword,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @RequestParam(name = "confirmPassword") String confirmPassword,
                                 Model model) {
        user = userService.getAuthorizedUser();
        model.addAttribute("user", user);
        if (!userService.passwordMatches(currentPassword)) {
            model.addAttribute("currentPasswordError", "Вы ввели неверный пароль");
            return "/user/account";
        }
        return "redirect:/account";
    }
}
