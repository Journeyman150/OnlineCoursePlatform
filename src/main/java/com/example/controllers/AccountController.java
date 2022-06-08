package com.example.controllers;

import com.example.domain.Role;
import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

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

    @PatchMapping("/account/update")
    public String update(@RequestParam(name = "email") String email,
                         @RequestParam(name = "name") String name,
                         @RequestParam(name = "surname") String surname,
                         Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        model.addAttribute("user", user);
        boolean thereAreError = false;
        if (!email.matches("(\\S{1,15})@(\\S{1,15})\\.(\\S{1,5})")) {
            model.addAttribute("emailErrorMessage", "Email should be correct");
            thereAreError = true;
        }
        if (name.length() < 2 || name.length() > 30) {
            model.addAttribute("nameErrorMessage", "Name should be between 2 and 30 characters.");
            thereAreError = true;
        }
        if (surname.length() < 2 || surname.length() > 30) {
            model.addAttribute("surnameErrorMessage", "Surname should be between 2 and 30 characters.");
            thereAreError = true;
        }
        if (!thereAreError) {
            user.setEmail(email);
            user.setName(name);
            user.setSurname(surname);
            userService.update(user.getId(), user);
            return "redirect:/account";
        }
        return "account";
    }

    @PatchMapping("/account/change_password")
    public String changePassword(@RequestParam(name = "currentPassword") String currentPassword,
                                 @RequestParam(name = "newPassword") String newPassword,
                                 @RequestParam(name = "confirmPassword") String confirmPassword,
                                 Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (!userService.passwordMatches(currentPassword, user)) {
            model.addAttribute("currentPasswordErrorMessage", "You entered wrong password.");
            return getAccountPage(model);
        }
        if (newPassword.length() < 7) {
            model.addAttribute("newPasswordErrorMessage", "Password length must be greater than 6 characters.");
            return getAccountPage(model);
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordErrorMessage", "Passwords do not match.");
            return getAccountPage(model);
        }
        userService.changePassword(user.getId(), newPassword);
        model.addAttribute("successMessage", "Password changed successfully.");
        return getAccountPage(model);
    }

    @DeleteMapping("/account")
    public String deleteAccount(Model model,
                                HttpServletRequest request) throws ServletException {
        User user = userService.getAuthorizedUser();
        if (!user.getAuthorities().contains(Role.USER)) {
            model.addAttribute("message",
                    "You can't delete your account, because your role is " +
                    user.getAuthorities().stream().findAny().orElse(null));
            return getAccountPage(model);
        }
        userService.delete(user.getId());
        request.logout();
        return "home";
    }
}
