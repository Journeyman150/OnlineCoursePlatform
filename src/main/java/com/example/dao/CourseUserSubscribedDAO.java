package com.example.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CourseUserSubscribedDAO {
    private JdbcTemplate jdbcTemplate;
    @Autowired
    public CourseUserSubscribedDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Long> getCoursesIdListByUserId(long userId) {
        return jdbcTemplate.query("SELECT course_id FROM course_usr WHERE usr_id=?",
                (rs, rowNum) -> rs.getLong("course_id"), userId);
    }

    public void subscribe(long courseId, long userId) {
        jdbcTemplate.update("INSERT INTO course_usr VALUES (?, ?)", courseId, userId);
    }
}
