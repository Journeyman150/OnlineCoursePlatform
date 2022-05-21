package com.example.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseInvitationDAO {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public CourseInvitationDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addInvitation(long courseId, long userId) {
        jdbcTemplate.update("INSERT INTO invitations (course_id, usr_id) VALUES (?, ?)", courseId, userId);
    }

    public List<Long> getInvitedUsersIdListByCourseId(long courseId) {
        return jdbcTemplate.query("SELECT usr_id FROM invitations WHERE course_id = ?",
                (rs, rowNum) -> rs.getLong("usr_id"),
                courseId);
    }

    public List<Long> getCoursesIdListByInvitedUserId(long userId) {
        return jdbcTemplate.query("SELECT course_id FROM invitations WHERE usr_id = ?",
                (rs, rowNum) -> rs.getLong("course_id"),
                userId);
    }
}
