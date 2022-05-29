package com.example.controllers.api;

import com.example.domain.User;
import com.example.exception_handling.api.InvalidUserDataException;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountRESTController {
    private final UserService userService;

    @Autowired
    public AccountRESTController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public User getAuthorizedUser() {
        return userService.getUserById(userService.getAuthorizedUser().getId());
    }

    @PatchMapping("/update")
    public User updateUserData(@RequestBody User updatedUser) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (!updatedUser.getEmail().matches("(\\S{1,15})@(\\S{1,15})\\.(\\S{1,5})")) {
            throw new InvalidUserDataException("Email should be correct");
        }
        if (updatedUser.getName().length() < 2 || updatedUser.getName().length() > 30) {
            throw new InvalidUserDataException("Name should be between 2 and 30 characters.");
        }
        if (updatedUser.getSurname().length() < 2 || updatedUser.getSurname().length() > 30) {
            throw new InvalidUserDataException("Surname should be between 2 and 30 characters.");
        }
        user.setEmail(updatedUser.getEmail());
        user.setName(updatedUser.getName());
        user.setSurname(updatedUser.getSurname());
        userService.update(user.getId(), user);
        return user;
    }

    @PatchMapping("/change_password")
    public User changePassword(@RequestBody List<String> passwords) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (!userService.passwordMatches(passwords.get(0), user)) {
            throw new InvalidUserDataException("Passed password does not match the old password.");
        }
        if (passwords.get(1).length() < 7) {
            throw new InvalidUserDataException("Password length must be greater than 6 characters.");
        }
        userService.changePassword(user.getId(), passwords.get(1));
        return user;
    }
}
