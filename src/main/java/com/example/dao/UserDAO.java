package com.example.dao;

import com.example.domain.User;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserDAO {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    public List<User> getUsersList() {
        return jdbcTemplate.query("SELECT * FROM Usr ORDER BY email", userMapper);
    }

    @Nullable
    public User getUserById(Long id) {
        return jdbcTemplate.query("SELECT * FROM usr WHERE usr_id = ?", userMapper, id)
                .stream().findAny().orElse(null);
    }

    @Nullable
    public User getUserByEmail(String email) {
        return jdbcTemplate.query("SELECT * FROM usr WHERE email = ?", userMapper, email)
                .stream().findAny().orElse(null);
    }

    public void addUser(User user) {
        jdbcTemplate.update("INSERT INTO usr(name, surname, email, password) VALUES(?, ?, ?, ?)",
                user.getName(), user.getSurname(), user.getEmail(), user.getPassword());
    }

    public void updateUser(long userId, User updatedUser) {
        jdbcTemplate.update("UPDATE usr SET name=?, surname=?, email=? WHERE usr_id=?",
                updatedUser.getName(),
                updatedUser.getSurname(),
                updatedUser.getEmail(),
                userId);
    }

    public void changeUserPassword(long userId, String password) {
        jdbcTemplate.update("UPDATE usr SET password=? WHERE usr_id=?", password, userId);
    }

    public void changeUserRole(long userId, String role) {
        jdbcTemplate.update("UPDATE usr SET role=? WHERE usr_id=?", role, userId);
    }

    public void updateUserBalance(long userId, int balance) {
        jdbcTemplate.update("UPDATE usr SET balance=? WHERE usr_id=?", balance, userId);
    }

    public void deleteUser(long userId) {
        jdbcTemplate.update("DELETE FROM Usr WHERE usr_id=?", userId);
    }

    public List<IndexedData> getSearchDataList() {
        return jdbcTemplate.query("SELECT usr_id, name, surname, email FROM usr",
                (rs, rowNum) -> new IndexedData(rs.getLong("usr_id"),
                        rs.getString("name"), rs.getString("surname"), rs.getString("email")));
    }
}
