package com.example.controllers;

import com.example.dao.UserDAO;
import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping("/account/change_password")
    public String changePassword(@RequestParam(name = "currentPassword") String currentPassword,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @RequestParam(name = "confirmPassword") String confirmPassword,
                                 Model model) {
        User user = userService.getAuthorizedUser();
        model.addAttribute("user", user);
        if (!userService.passwordMatches(currentPassword)) {
            model.addAttribute("currentPasswordErrorMessage", "Вы ввели неверный пароль.");
            return "/user/account";
        }
        if (newPassword.length() <= 6) {
            model.addAttribute("newPasswordErrorMessage", "Пароль должен быть длиньше 6 смволов.");
            return "/user/account";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordErrorMessage", "Пароли не совпадат.");
            return "/user/account";
        }
        userDAO.changeUserPassword(user.getId(), userService.getEncodedPassword(newPassword));
        return "redirect:/account";
    }
}
