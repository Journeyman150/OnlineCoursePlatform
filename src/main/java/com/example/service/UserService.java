package com.example.service;

import com.example.dao.UserDAO;
import com.example.domain.Course;
import com.example.domain.Role;
import com.example.domain.User;
import com.example.search_engine.IndexedData;
import com.example.search_engine.UsersSearchData;
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
    private List<User> filteredUsersList;
    private UsersSearchData usersSearchData;

    @Autowired
    public UserService(UserDAO userDAO,
                       PasswordEncoder passwordEncoder,
                       UsersSearchData usersSearchData) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.usersSearchData = usersSearchData;
        filteredUsersList = new ArrayList<>();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userDAO.getUserByEmail(email);
    }

    @Nullable
    public Long findUserId(String email) {
        User user = userDAO.getUserByEmail(email);
        if (user == null) {
            return null;
        } else return user.getId();
    }

    public User getUserById(long userId) {
        return userDAO.getUserById(userId);
    }

    public User getAuthorizedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public boolean isUserAlreadyExist(String email) {
        return userDAO.getUserByEmail(email) != null;
    }

    public List<User> getUsersList() {
        return userDAO.getUsersList();
    }

    public List<User> getFilteredUsersList(String keyword) {
        filteredUsersList.clear();
        Set<Long> idxes = usersSearchData.findIndexes(keyword);
        System.out.println(idxes);
        if (!idxes.contains(-1L))
            idxes.forEach(n -> filteredUsersList.add(userDAO.getUserById(n)));
        return filteredUsersList;
    }

    @Transactional
    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        long userId = userDAO.save(user);
        usersSearchData.writeData(userId, user.getEmail(), user.getName(), user.getSurname());
    }

    @Transactional
    public void update(long userId, User updatedUser) {
        User oldUser = userDAO.getUserById(userId);
        usersSearchData.deleteData(userId, oldUser.getEmail(), oldUser.getName(), oldUser.getSurname());
        usersSearchData.writeData(userId, updatedUser.getEmail(), updatedUser.getName(), updatedUser.getSurname());
        userDAO.updateUser(userId, updatedUser);
    }

    public void changePassword(long userId, String newPassword) {
        userDAO.changeUserPassword(userId, passwordEncoder.encode(newPassword));
    }

    public void changeRole(long userId, String role) {
        userDAO.changeUserRole(userId, role);
    }

    public void updateUserBalance(long userId, int balance) {
        userDAO.updateUserBalance(userId, balance);
    }

    @Transactional
    public void delete(long userId) {
        User user = userDAO.getUserById(userId);
        usersSearchData.deleteData(userId, user.getEmail(), user.getName(), user.getSurname());
        userDAO.deleteUser(userId);
    }

    public boolean passwordMatches(String password, User user) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public List<IndexedData> getSearchDataList() {
        return userDAO.getSearchDataList();
    }


}
