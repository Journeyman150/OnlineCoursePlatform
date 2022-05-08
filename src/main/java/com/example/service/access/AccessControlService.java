package com.example.service.access;

import com.example.dao.CourseUserSubscribedDAO;
import com.example.domain.User;
import com.example.service.CourseSubscribingUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccessControlService {
    private IdCheckService idCheckService;
    private CourseUserSubscribedDAO courseUserSubscribedDAO;
    @Autowired
    public AccessControlService(IdCheckService idCheckService,
                                CourseUserSubscribedDAO courseUserSubscribedDAO) {
        this.idCheckService = idCheckService;
        this.courseUserSubscribedDAO = courseUserSubscribedDAO;
    }
    public boolean authorHasAccessToCourse(User author, long courseId) {
        return idCheckService.getAuthorIdByCourseId(courseId) == author.getId();
    }
    public boolean userHasAccessToCourse(long userId, long courseId) {
        List<Long> coursesIdList = courseUserSubscribedDAO.getCoursesIdListByUserId(userId);
        return coursesIdList.contains(courseId);
    }
}
