package com.example.dao;

import com.example.domain.Course;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseDAO {
    private JdbcTemplate jdbcTemplate;
    private CourseMapper courseMapper;
    @Autowired
    public CourseDAO(JdbcTemplate jdbcTemplate, CourseMapper courseMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.courseMapper = courseMapper;
    }
    public List<Course> getAllCourses() {
        return jdbcTemplate.query("SELECT * FROM courses", courseMapper);
    }

    public List<Course> getAllPublicCourses() {
        return jdbcTemplate.query("SELECT * FROM courses WHERE non_public=false", courseMapper);
    }

    public List<Course> getAllPrivateCourses() {
        return jdbcTemplate.query("SELECT * FROM courses WHERE non_public=true", courseMapper);
    }

    public List<Course> getAllCoursesByAuthorId(long authorId) {
        return jdbcTemplate.query("SELECT * FROM courses WHERE author_id=?", courseMapper, authorId);
    }

    public List<Course> getPublicCoursesByAuthorId(long authorId) {
        return jdbcTemplate.query("SELECT * FROM courses WHERE author_id=? AND non_public=false", courseMapper, authorId);
    }

    public List<Course> getNonPublicCoursesByAuthorId(long authorId) {
        return jdbcTemplate.query("SELECT * FROM courses WHERE author_id=? AND non_public=true", courseMapper, authorId);
    }

    @Nullable
    public Course getCourseById(long courseId) {
        return jdbcTemplate.query("SELECT * FROM courses WHERE course_id=?", courseMapper, courseId)
                .stream().findAny().orElse(null);
    }
    @Nullable
    public Course getPublicCourseById(long courseId) {
        return jdbcTemplate.query("SELECT * FROM courses WHERE course_id=? AND non_public=false", courseMapper, courseId)
                .stream().findAny().orElse(null);
    }
    @Nullable
    public Course getNonPublicCourseById(long courseId) {
        return jdbcTemplate.query("SELECT * FROM courses WHERE course_id=? AND non_public=true", courseMapper, courseId)
                .stream().findAny().orElse(null);
    }
    @Nullable
    public Long getAuthorIdByCourseId(long courseId) {
        return jdbcTemplate.query("SELECT author_id FROM courses WHERE course_id = ?",
                (rs, rowNum) -> rs.getLong("author_id"),
                courseId)
                .stream().findAny().orElse(null);
    }

    public long save(Course course) {
        return jdbcTemplate.queryForObject("INSERT INTO courses(title, description, author_id, price, non_public) " +
                                            "VALUES(?, ?, ?, ?, ?) " +
                                            "RETURNING course_id",
                                        Long.class,
                                        course.getTitle(),
                                        course.getDescription(),
                                        course.getAuthorId(),
                                        course.getPrice(),
                                        course.isNonPublic());
    }

    public void update(Course updatedCourse, long courseId) {
        jdbcTemplate.update("UPDATE courses SET title=?, description=?, price=?, non_public=? WHERE course_id=?",
                updatedCourse.getTitle(),
                updatedCourse.getDescription(),
                updatedCourse.getPrice(),
                updatedCourse.isNonPublic(),
                courseId);
    }

    public void delete(long courseId) {
        jdbcTemplate.update("DELETE FROM courses WHERE course_id=?", courseId);
    }

    public List<IndexedData> getPublicCoursesSearchDataList() {
        return jdbcTemplate.query("SELECT course_id, title, description FROM courses WHERE non_public = false",
                (rs, rowNum) -> new IndexedData(rs.getLong("course_id"),
                        rs.getString("title"), rs.getString("description")));
    }
}
