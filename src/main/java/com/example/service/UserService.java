package com.example.service;

import com.example.dao.UserDAO;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {
    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private List<User> usersList;
    private List<User> filteredUsersList;

    @Autowired
    public UserService(UserDAO userDAO, PasswordEncoder passwordEncoder) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        usersList = userDAO.getUsersList();
        filteredUsersList = new ArrayList<>();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userDAO.getUserByEmail(email);
    }

    public User getAuthorizedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean userAlreadyExist(String email) {
        if (userDAO.getUserByEmail(email) != null) {
            return true;
        } else {
            return false;
        }
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public void refreshUsersList() {
        usersList = userDAO.getUsersList();
    }

    @Nullable
    public User getUserFromListById(long id) {
        return usersList.stream().filter(n -> n.getId() == id).findAny().orElse(null);
    }

    public List<User> getFilteredUsersList(String keyword) {
        filteredUsersList.clear();
        for (User user: usersList) {
            if (user.getEmail().lastIndexOf(keyword) != -1 ||
                (user.getName() + user.getSurname()).lastIndexOf(keyword) != -1) {
                filteredUsersList.add(user);
            }
        }
        return filteredUsersList;
    }

    public void updateUser(long id, User updatedUser) {
        for (User user: usersList) {
            if (user.getId() == id) {
                user.setAll(updatedUser);
            }
        }
        userDAO.updateUser(id, updatedUser);
    }

    public String getEncodedPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean passwordMatches(String password) {
        User user = getAuthorizedUser();
        if (passwordEncoder.matches(password, user.getPassword())) {
            return true;
        } else return false;
    }


}
