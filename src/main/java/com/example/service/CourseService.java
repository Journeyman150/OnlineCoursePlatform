package com.example.service;

import com.example.dao.CourseDAO;
import com.example.domain.Course;
import com.example.domain.User;
import com.example.search_engine.CoursesSearchData;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.*;

@Service
public class CourseService {
    @Value("${storage.root.path}")
    private String storagePathPrefix;
    private final CourseDAO courseDAO;
    private final CoursesSearchData coursesSearchData;
    private final StorageService storageService;
    private final UserService userService;

    @Autowired
    public CourseService(CourseDAO courseDAO,
                         CoursesSearchData coursesSearchData,
                         StorageService storageService,
                         UserService userService) {
        this.courseDAO = courseDAO;
        this.coursesSearchData = coursesSearchData;
        this.storageService = storageService;
        this.userService = userService;
    }

    public List<Course> findCourses(String keyword) {
        if (keyword == null || keyword.matches("\s*")) {
            return new ArrayList<>();
        }
        Set<Long> set = coursesSearchData.findIndexes(CoursesSearchData.getSeparateKeywords(keyword));
        Set<Long> set2 = this.getPublicCoursesIdSetByAuthorsId(userService.getUserIdSet(keyword));
        set.addAll(set2);
        if (set.size() == 1 && set.contains(-1L)) {
            return new ArrayList<>();
        }
        String indexesStr = set.toString();
        return courseDAO.getCoursesListByIndexes(indexesStr.substring(1, indexesStr.length() - 1));
    }

    public Page<Course> getPaginated(List<Course> coursesList, Pageable pageable) {
        if (coursesList == null || coursesList.isEmpty()) {
            return new PageImpl<Course>(Collections.emptyList(), PageRequest.of(1, 1), 0);
        }
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

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

    public List<Course> getAllCourses() {
        return courseDAO.getAllCourses();
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

    public Set<Long> getPublicCoursesIdSetByAuthorsId(Set<Long> authorsId) {
        Set<Long> coursesSet = new HashSet<>();
        authorsId.forEach(n -> coursesSet.addAll(courseDAO.getPublicCoursesIdByAuthorId(n)));
        return coursesSet;
    }

    @Transactional
    public long save(Course course, MultipartFile courseIcon) {
        long courseId = courseDAO.save(course);
        if (courseIcon != null && !courseIcon.isEmpty()) {
            String storagePath = storagePathPrefix + "/" + courseId;
            String imageSource = storagePath + "/" + "icon";
            courseDAO.updateIconSource(imageSource, courseId);
            storageService.store(courseIcon, storagePath, imageSource);
        }
        if (!course.isNonPublic()) {
            coursesSearchData.writeData(courseId, course.getTitle(), course.getDescription());
        }
        return courseId;
    }

    @Transactional
    public void update(Course course, MultipartFile courseIcon, long courseId) {
        Course oldCourse = courseDAO.getCourseById(courseId);
        courseDAO.update(course, courseId);
        if (courseIcon != null && !courseIcon.isEmpty()) {
            String storagePath = storagePathPrefix + "/" + courseId;
            String imageSource = storagePath + "/" + "icon";
            courseDAO.updateIconSource(imageSource, courseId);
            storageService.store(courseIcon, storagePath, imageSource);
        }
        coursesSearchData.deleteData(courseId, oldCourse.getTitle(), oldCourse.getDescription());
        if (!course.isNonPublic()) {
            coursesSearchData.writeData(courseId, course.getTitle(), course.getDescription());
        }
    }

    @Transactional
    public void delete(long courseId) {
        Course course = courseDAO.getCourseById(courseId);
        courseDAO.delete(courseId);
        coursesSearchData.deleteData(courseId, course.getTitle(), course.getDescription());
    }

    public List<IndexedData> getPublicCoursesSearchDataList() {
        return courseDAO.getPublicCoursesSearchDataList();
    }

    public File getCourseIcon(String iconSource) {
        return storageService.load(iconSource);
    }
}
