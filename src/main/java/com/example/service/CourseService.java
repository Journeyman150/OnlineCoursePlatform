package com.example.service;

import com.example.dao.CourseDAO;
import com.example.domain.Course;
import com.example.domain.User;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseDAO courseDAO;

    @Autowired
    public CourseService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
    }

    public List<Course> getPublicListByAuthorId(long authorId) {
        return courseDAO.getPublicCoursesByAuthorId(authorId);
    }

    public List<Course> getNonPublicListByAuthorId(long authorId) {
        return courseDAO.getNonPublicCoursesByAuthorId(authorId);
    }
    @Nullable
    public Course getCourseById(long courseId) {
        return courseDAO.getCourseById(courseId);
    }
    @Nullable
    public Course getNonPublicCourseById(long courseId) {
        return courseDAO.getNonPublicCourseById(courseId);
    }
    @Nullable
    public Course getPublicCourseById(long courseId) {
        return courseDAO.getPublicCourseById(courseId);
    }

    public void save(Course course, User author) {
        course.setAuthorId(author.getId());
        courseDAO.save(course);
    }

    public void update(Course course, long courseId) {
        courseDAO.update(course, courseId);
    }

    public List<IndexedData> getPublicCoursesSearchDataList() {
        return courseDAO.getPublicCoursesSearchDataList();
    }
}
