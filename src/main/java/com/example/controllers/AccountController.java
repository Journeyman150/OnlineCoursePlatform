package com.example.controllers;

import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AccountController {
    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account")
    public String getAccountPage(Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        model.addAttribute("user", user);
        return "account";
    }

    @PatchMapping("/account/change_password")
    public String changePassword(@RequestParam(name = "currentPassword") String currentPassword,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @RequestParam(name = "confirmPassword") String confirmPassword,
                                 Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        model.addAttribute("user", user);
        if (!userService.passwordMatches(currentPassword, user)) {
            model.addAttribute("currentPasswordErrorMessage", "You entered the wrong password.");
            return "account";
        }
        if (newPassword.length() < 7) {
            model.addAttribute("newPasswordErrorMessage", "Password length must be greater than 6 characters.");
            return "account";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordErrorMessage", "Passwords do not match.");
            return "account";
        }
        userService.changePassword(user.getId(), newPassword);
        return "redirect:/account";
    }
}
