package com.example.dao;

import com.example.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseSubscribeDAO {
    private JdbcTemplate jdbcTemplate;
    private CourseMapper courseMapper;
    @Autowired
    public CourseSubscribeDAO(JdbcTemplate jdbcTemplate,
                              CourseMapper courseMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.courseMapper = courseMapper;
    }

    public List<Long> getCoursesIdListByUserId(long userId) {
        return jdbcTemplate.query("SELECT course_id FROM course_usr WHERE usr_id=?",
                (rs, rowNum) -> rs.getLong("course_id"), userId);
    }

    public List<Course> getCoursesListBySubscribedUserId(long userId) {
        return jdbcTemplate.query("SELECT courses.* FROM courses " +
                                        "INNER JOIN course_usr " +
                                        "ON courses.course_id = course_usr.course_id " +
                                        "WHERE course_usr.usr_id = ?",
                                    courseMapper, userId);
    }

    public void subscribe(long courseId, long userId) {
        jdbcTemplate.update("INSERT INTO course_usr VALUES (?, ?)", courseId, userId);
    }

    public long getSubscribedUsersNumberByCourseId(long courseId) {
        return jdbcTemplate.query("SELECT course_id, COUNT(*) " +
                        "FROM course_usr WHERE course_id = ? " +
                        "GROUP BY course_id",
                        (rs, n) -> rs.getLong("count"),
                        courseId)
                        .stream().findAny().orElse(0L);
    }
}
