package com.example.service;

import com.example.dao.CourseDAO;
import com.example.domain.Course;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseService {
    private final CourseDAO courseDAO;
    private Map<Long, Course> publicCoursesMap;
    private Map<Long, Course> nonPublicCoursesMap;

    @Autowired
    public CourseService(CourseDAO courseDAO) {
        this.courseDAO = courseDAO;
        publicCoursesMap = new HashMap<>();
        nonPublicCoursesMap = new HashMap<>();
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
        if (course.isNonPublic()) {
            nonPublicCoursesMap.put(course.getId(), course);
        } else {
            publicCoursesMap.put(course.getId(), course);
        }
    }

}
