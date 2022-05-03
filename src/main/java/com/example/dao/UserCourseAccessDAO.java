package com.example.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserCourseAccessDAO {
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    public UserCourseAccessDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public List<Long> getAccessibleCoursesIdListByUserId(long userId) {
        return jdbcTemplate.query("SELECT course_id FROM course_usr WHERE usr_id = ?",
                (rs, rowNum) -> rs.getLong("course_id"),
                userId);
    }
}
