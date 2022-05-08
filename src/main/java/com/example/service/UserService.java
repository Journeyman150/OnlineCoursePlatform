package com.example.service;

import com.example.dao.UserDAO;
import com.example.domain.Course;
import com.example.domain.Role;
import com.example.domain.User;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public User getUserById(long userId) {
        return userDAO.getUserById(userId);
    }

    @Nullable
    public User getUserFromListById(long userId) {
        return usersList.stream().filter(n -> n.getId() == userId).findAny().orElse(null);
    }

    public User getAuthorizedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean isUserAlreadyExist(String email) {
        return userDAO.getUserByEmail(email) != null;
    }

    public List<User> getUsersList() {
        return usersList;
    }

    public void refreshUsersList() {
        usersList = userDAO.getUsersList();
    }

    public List<User> getFilteredUsersList(String keyword) {
        filteredUsersList.clear();
        for (User user: usersList) {
            if (user.getEmail().contains(keyword) ||
                (user.getName() + user.getSurname()).contains(keyword)) {
                filteredUsersList.add(user);
            }
        }
        return filteredUsersList;
    }
    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDAO.addUser(user);
    }

    public void update(long userId, User updatedUser) {
        for (int i = 0; i < usersList.size(); i++) {
            if (usersList.get(i).getId() == userId) {
                usersList.set(i, updatedUser);
            }
        }
        userDAO.updateUser(userId, updatedUser);
    }

    public void changePassword(long userId, String newPassword) {
        userDAO.changeUserPassword(userId, passwordEncoder.encode(newPassword));
    }

    public boolean passwordMatches(String password, User user) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void changeRole(long userId, String role) {
        for (User user : usersList) {
            if (user.getId() == userId) {
                user.setAuthorities(Set.of(Role.valueOf(role)));
            }
        }
        userDAO.changeUserRole(userId, role);
    }
    public List<IndexedData> getSearchDataList() {
        return userDAO.getSearchDataList();
    }
}
