package com.example.dao;

import com.example.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    public Map<Long, Long> getTopPublicCoursesIdMapToSubsCount(int limit) {
        Map<Long, Long> courseIdSubsMap = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT t1.course_id, count(*) FROM course_usr AS t1 " +
                "INNER JOIN courses ON t1.course_id = courses.course_id " +
                "WHERE non_public = false " +
                "GROUP BY t1.course_id " +
                "ORDER BY COUNT(*) DESC " +
                "LIMIT(?)", (rs, n) -> {
            courseIdSubsMap.put(rs.getLong("course_id"), rs.getLong("count"));
            return null;
        }, limit);
        return courseIdSubsMap;
    }
}
