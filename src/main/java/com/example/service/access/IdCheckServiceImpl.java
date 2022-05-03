package com.example.service.access;

import com.example.dao.CourseDAO;
import com.example.dao.LessonDAO;
import com.example.dao.UserCourseAccessDAO;
import com.example.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IdCheckServiceImpl implements IdCheckService {
    private final UserDAO userDAO;
    private final CourseDAO courseDAO;
    private final LessonDAO lessonDAO;
    private final UserCourseAccessDAO userCourseAccessDAO;

    @Autowired
    public IdCheckServiceImpl(UserDAO userDAO, CourseDAO courseDAO, LessonDAO lessonDAO, UserCourseAccessDAO userCourseAccessDAO) {
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.lessonDAO = lessonDAO;
        this.userCourseAccessDAO = userCourseAccessDAO;
    }

    @Override
    public long getAuthorIdByCourseId(long courseId) {
        return courseDAO.getAuthorIdByCourseId(courseId);
    }

    @Override
    public long getCourseIdByLessonId(long lessonId) {
        return lessonDAO.getCourseIdByLessonId(lessonId);
    }

    @Override
    public List<Long> getAccessibleCoursesIdListByUserId(long userId) {
        return userCourseAccessDAO.getAccessibleCoursesIdListByUserId(userId);
    }
}
