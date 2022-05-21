package com.example.service.access;

import com.example.dao.CourseInvitationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CourseInvitationService {
    private CourseInvitationDAO courseInvitationDAO;

    @Autowired
    public CourseInvitationService(CourseInvitationDAO courseInvitationDAO) {
        this.courseInvitationDAO = courseInvitationDAO;
    }

    public void addInvitation(long courseId, long userId) {
        courseInvitationDAO.addInvitation(courseId, userId);
    }

    public List<Long> getInvitedUsersIdListByCourseId(long courseId) {
        return courseInvitationDAO.getInvitedUsersIdListByCourseId(courseId);
    }

    public List<Long> getCoursesIdListByInvitedUserId(long userId) {
        return courseInvitationDAO.getCoursesIdListByInvitedUserId(userId);
    }
}
