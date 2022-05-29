package com.example.controllers.api;

import com.example.domain.User;
import com.example.exception_handling.api.InvalidUserDataException;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class RegistrationRESTController {
    private final UserService userService;

    @Autowired
    public RegistrationRESTController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/registration")
    public User createUser(@RequestBody User user) {
        user.setEmail(user.getEmail().toLowerCase());
        if (userService.isUserAlreadyExist(user.getEmail())) {
            throw new InvalidUserDataException("User with entered email already exists.");
        }
        if (!user.getEmail().matches("(\\S{1,15})@(\\S{1,15})\\.(\\S{1,5})")) {
            throw new InvalidUserDataException("Email should be correct");
        }
        if (user.getName().length() < 2 || user.getName().length() > 30) {
            throw new InvalidUserDataException("Name should be between 2 and 30 characters.");
        }
        if (user.getSurname().length() < 2 || user.getSurname().length() > 30) {
            throw new InvalidUserDataException("Surname should be between 2 and 30 characters.");
        }
        if (user.getPassword().length() < 7) {
            throw new InvalidUserDataException("Password length must be greater than 6 characters.");
        }
        long userId = userService.save(user);
        return userService.getUserById(userId);
    }
}
