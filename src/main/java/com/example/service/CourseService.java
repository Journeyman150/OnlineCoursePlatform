package com.example.service;

import com.example.dao.CourseDAO;
import com.example.domain.Course;
import com.example.domain.User;
import com.example.search_engine.CoursesSearchData;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
public class CourseService {
    private final CourseDAO courseDAO;
    private final CoursesSearchData coursesSearchData;

    @Autowired
    public CourseService(CourseDAO courseDAO,
                         CoursesSearchData coursesSearchData) {
        this.courseDAO = courseDAO;
        this.coursesSearchData = coursesSearchData;
    }

    public Page<Course> findPaginated(String keyword, Pageable pageable) {
        if (keyword == null || keyword.equals("") || keyword.matches("\s+")) {
            return new PageImpl<Course>(Collections.emptyList(), PageRequest.of(1, 1), 0);
        }
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Course> coursesList = new ArrayList<>();
        Set<Long> set = coursesSearchData.findIndexes(CoursesSearchData.getSeparateKeywords(keyword));
        if (set.contains(-1L)) {
            return new PageImpl<Course>(Collections.emptyList(), PageRequest.of(1, 1), 0);
        }
        set.forEach(n -> coursesList.add(this.getCourseById(n)));

        List<Course> list;

        if (coursesList.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, coursesList.size());
            list = coursesList.subList(startItem, toIndex);
        }

        Page<Course> coursesPage
                = new PageImpl<Course>(list, PageRequest.of(currentPage, pageSize), coursesList.size());

        return coursesPage;
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
