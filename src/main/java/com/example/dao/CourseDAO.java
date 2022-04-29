package com.example.dao;

import com.example.domain.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    public void saveCourse(Course course) {
        jdbcTemplate.update("INSERT INTO courses(title, description, author_id, price, non_public) VALUES(?, ?, ?, ?, ?)",
                course.getTitle(),
                course.getDescription(),
                course.getAuthorId(),
                course.getPrice(),
                course.isNonPublic());
    }

    public void updateCourse(Course updatedCourse, long courseId) {
        jdbcTemplate.update("UPDATE courses SET title=?, description=?, author_id=?, price=?, non_public=? WHERE course_id=?",
                updatedCourse.getTitle(),
                updatedCourse.getDescription(),
                updatedCourse.getAuthorId(),
                updatedCourse.getPrice(),
                updatedCourse.isNonPublic(),
                courseId);
    }
}
