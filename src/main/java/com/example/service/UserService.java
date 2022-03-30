package com.example.service;

import com.example.dao.UserDAO;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userDAO.getUserByEmail(email);
    }

    public boolean userAlreadyExist(String email) {
        if (userDAO.getUserByEmail(email) != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getEncodedPassword(String uncodedPassword) {
        return passwordEncoder.encode(uncodedPassword);
    }

    public boolean passwordMatches(String password) {
        User user = getAuthorizedUser();
        if (passwordEncoder.matches(password, user.getPassword())) {
            return true;
        } else return false;
    }

    public User getAuthorizedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
