package com.example.configs;

import com.example.search_engine.CoursesSearchData;
import com.example.search_engine.IndexedData;
import com.example.search_engine.LessonsSearchData;
import com.example.search_engine.UsersSearchData;
import com.example.service.CourseService;
import com.example.service.LessonService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SearchConfig {
    private UserService userService;
    private CourseService courseService;
    private LessonService lessonService;

    private UsersSearchData usersSearchData;
    private CoursesSearchData coursesSearchData;
    private LessonsSearchData lessonsSearchData;


    @Autowired
    public SearchConfig(UserService userService, CourseService courseService, LessonService lessonService,
                        UsersSearchData usersSearchData, CoursesSearchData coursesSearchData, LessonsSearchData lessonsSearchData) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.usersSearchData = usersSearchData;
        this.coursesSearchData = coursesSearchData;
        this.lessonsSearchData = lessonsSearchData;
    }

    @PostConstruct
    protected void init() {
        loadUsersSearchData();
        loadPublicCoursesSearchData();
        loadLessonsSearchData();
    }

    private void loadUsersSearchData() {
        List<IndexedData> usersDataList = userService.getSearchDataList();
        usersDataList.parallelStream().forEach(n -> usersSearchData.writeData(n.getIdx(), n.getDataArr()));
    }
    private void loadPublicCoursesSearchData() {
        List<IndexedData> coursesDataList = courseService.getPublicCoursesSearchDataList();
        coursesDataList.parallelStream().forEach(n -> coursesSearchData.writeData(n.getIdx(), n.getDataArr()));
    }
    private void loadLessonsSearchData() {
        List<IndexedData> lessonsDataList = lessonService.getSearchDataList();
        lessonsDataList.parallelStream().forEach(n -> lessonsSearchData.writeData(n.getIdx(), n.getDataArr()));
    }
}
