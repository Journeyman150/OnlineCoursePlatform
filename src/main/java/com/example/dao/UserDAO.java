package com.example.dao;

import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDAO {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        userMapper = new UserMapper();
    }

    public List<User> getUserList() {
        return jdbcTemplate.query("SELECT * FROM Usr", new UserMapper());
    }

    @Nullable
    public User getUserById(Long id) {
        return jdbcTemplate.query("SELECT * FROM Usr WHERE id = ?", userMapper).stream().findAny().orElse(null);
    }

    @Nullable
    public User getUserByEmail(String email) {
        return jdbcTemplate.query("SELECT * FROM Usr WHERE email = ?", userMapper, email)
                .stream().findAny().orElse(null);
    }

    public void addUser(User user) {
        jdbcTemplate.update("INSERT INTO Usr(name, surname, email, password) VALUES(?, ?, ?, ?)",
                user.getName(), user.getSurname(), user.getEmail(), user.getPassword());
    }

    public void updateUser(long id, User updatedUser) {
        jdbcTemplate.update("UPDATE Usr SET name=?, surname=?, email=? WHERE id=?",
                updatedUser.getName(), updatedUser.getSurname(), updatedUser.getEmail(), id);
    }

    public void deleteUser(long id) {
        jdbcTemplate.update("DELETE FROM Usr WHERE id=?", id);
    }

    public void changePassword(long id, String password) {
        jdbcTemplate.update("UPDATE Usr SET password=? WHERE id=?", password, id);
    }
}
