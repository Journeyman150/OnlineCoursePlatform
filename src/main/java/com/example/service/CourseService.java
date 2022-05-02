package com.example.service;

import com.example.dao.CourseDAO;
import com.example.domain.Course;
import com.example.domain.User;
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

    public void save(Course course, User author) {
        course.setAuthorId(author.getId());
        courseDAO.saveCourse(course);
    }

}
