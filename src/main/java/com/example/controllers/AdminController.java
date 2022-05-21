package com.example.controllers;

import com.example.dao.UserDAO;
import com.example.domain.Role;
import com.example.domain.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserDAO userDAO;
    private final UserService userService;

    @Autowired
    public AdminController(UserDAO userDAO, UserService userService) {
        this.userDAO = userDAO;
        this.userService = userService;
    }

    @GetMapping("/users_list")
    public String getUsersList(@RequestParam(name = "keyword", required = false) String keyword,
                               Model model) {
        if (keyword != null && !keyword.equals("")) {
            model.addAttribute("usersList", userService.getFilteredUsersList(keyword));
            return "admin/users_list";
        } else {
            model.addAttribute("usersList", userService.getUsersList());
            return "admin/users_list";
        }
    }

    @PostMapping("/users_list")
    public String getRefreshedUsersList() {
        userService.refreshUsersList();
        return "redirect:/admin/users_list";
    }

    @GetMapping("/user/{id}")
    public String getUserById(@PathVariable("id") long id,
                              Model model) {
        model.addAttribute("user", userDAO.getUserById(id));
        return "admin/user";
    }

    @GetMapping("/user/{id}/edit")
    public String getUserEditEditForm(@PathVariable("id") long id,
                           Model model) {
        model.addAttribute("user", userDAO.getUserById(id));
        return "admin/user_edit";
    }

    @PatchMapping("/user/{id}/edit")
    public String editUser(@PathVariable("id") long id,
                           @ModelAttribute("user") @Valid User user, BindingResult bindingResult) {
        user.setPassword(userDAO.getUserById(id).getPassword());
        if (bindingResult.hasFieldErrors("name") | bindingResult.hasFieldErrors("surname") | bindingResult.hasFieldErrors("email")) {
            return "admin/user_edit";
        }
        userService.update(id, user);
        return "redirect:/admin/user/" + id;
    }

    @PatchMapping("/user/{id}/change_password")
    public String changeUserPassword(@PathVariable("id") long id,
                                     @RequestParam(name = "newPassword") String newPassword,
                                     @RequestParam(name = "confirmPassword") String confirmPassword,
                                     @ModelAttribute("user") User user,
                                     Model model) {
        if (newPassword == null || newPassword.length() <= 6) {
            model.addAttribute("newPasswordErrorMessage", "Пароль должен быть длиньше 6 символов.");
            model.addAttribute("user", userService.getUserById(id));
            return "admin/user_edit";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("confirmPasswordErrorMessage", "Пароли не совпадают.");
            model.addAttribute("user", userService.getUserById(id));
            return "admin/user_edit";
        }
        userService.changePassword(id, newPassword);
        return "redirect:/admin/user/" + id;
    }

    @PatchMapping("/user/{id}/change_role")
    public String changeUserRole(@PathVariable("id") long id,
                                 @RequestParam(name = "role", required = true) String role,
                                 @ModelAttribute("user") User user) {
        userService.changeRole(id, role);
        return "redirect:/admin/user/" + id;
    }
}
