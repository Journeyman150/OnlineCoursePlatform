package com.example.dao;

import com.example.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseInvitationDAO {
    private final JdbcTemplate jdbcTemplate;
    private final CourseMapper courseMapper;

    @Autowired
    public CourseInvitationDAO(JdbcTemplate jdbcTemplate,
                               CourseMapper courseMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.courseMapper = courseMapper;
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

    public List<Course> getCoursesListByInvitedUserId(long userId) {
        return jdbcTemplate.query("SELECT courses.* FROM courses " +
                        "INNER JOIN invitations ON courses.course_id = invitations.course_id " +
                        "WHERE invitations.usr_id = ?",
                        courseMapper,
                        userId);
    }
}
