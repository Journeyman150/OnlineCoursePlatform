package com.example.service.access;

import com.example.dao.CourseSubscribeDAO;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessControlService {
    private IdCheckService idCheckService;
    private CourseSubscribeDAO courseSubscribeDAO;

    @Autowired
    public AccessControlService(IdCheckService idCheckService,
                                CourseSubscribeDAO courseSubscribeDAO) {
        this.idCheckService = idCheckService;
        this.courseSubscribeDAO = courseSubscribeDAO;
    }

    public boolean authorHasAccessToCourse(User author, long courseId) {
        return idCheckService.getAuthorIdByCourseId(courseId) == author.getId();
    }
    //Check user access to course lessons
    public boolean userHasAccessToCourse(long userId, long courseId) {
        List<Long> coursesIdList = courseSubscribeDAO.getCoursesIdListByUserId(userId);
        return coursesIdList.contains(courseId);
    }
}
