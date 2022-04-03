package com.example.dao;

import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    @Autowired
    public UserDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        userMapper = new UserMapper();
    }

    public List<User> getUsersList() {
        return jdbcTemplate.query("SELECT * FROM Usr ORDER BY email", userMapper);
    }

    @Nullable
    public User getUserById(Long id) {
        return jdbcTemplate.query("SELECT * FROM Usr WHERE usr_id = ?", userMapper, id)
                .stream().findAny().orElse(null);
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

    public void updateUser(long usr_id, User updatedUser) {
        jdbcTemplate.update("UPDATE Usr SET name=?, surname=?, email=?, password=? WHERE usr_id=?",
                updatedUser.getName(), updatedUser.getSurname(), updatedUser.getEmail(), updatedUser.getPassword(), usr_id);
    }

    public void deleteUser(long usr_id) {
        jdbcTemplate.update("DELETE FROM Usr WHERE usr_id=?", usr_id);
    }

    public void changeUserPassword(long usr_id, String password) {
        jdbcTemplate.update("UPDATE Usr SET password=? WHERE usr_id=?", password, usr_id);
    }

    public void changeUserRole(long usr_id, String role) {
        jdbcTemplate.update("UPDATE Usr SET role=? WHERE usr_id=?", role, usr_id);
    }
}
