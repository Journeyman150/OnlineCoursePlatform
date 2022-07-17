package com.example.service;

import com.example.dao.CourseDAO;
import com.example.dao.CourseSubscribeDAO;
import com.example.dao.UserDAO;
import com.example.domain.Course;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class CourseSubscribeService {
    private UserDAO userDAO;
    private CourseDAO courseDAO;
    private CourseSubscribeDAO courseSubscribeDAO;
    @Autowired
    public CourseSubscribeService(UserDAO userDAO, CourseDAO courseDAO, CourseSubscribeDAO courseSubscribeDAO) {
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.courseSubscribeDAO = courseSubscribeDAO;
    }

    @Transactional
    public void subscribeUserAndMakePayment(User user, User author, Course course) {
        this.makePayment(user, author, course.getPrice());
        this.subscribe(course.getId(), user.getId());
    }

    public long getSubscribedUsersNumberByCourseId(long courseId) {
        return courseSubscribeDAO.getSubscribedUsersNumberByCourseId(courseId);
    }

    public List<Long> getCoursesIdListByUserId(long userId) {
        return courseSubscribeDAO.getCoursesIdListByUserId(userId);
    }

    public List<Course> getCoursesListBySubscribedUserId(long userId) {
        return courseSubscribeDAO.getCoursesListBySubscribedUserId(userId);
    }

    public void subscribe(long courseId, long userId) {
        courseSubscribeDAO.subscribe(courseId, userId);
    }

    @Transactional
    public boolean makePayment(User payer, User beneficiary, int price) {
        int payerBalance = payer.getBalance() - price;
        int beneficiaryBalance = beneficiary.getBalance() + price;
        userDAO.updateUserBalance(payer.getId(), payerBalance);
        userDAO.updateUserBalance(beneficiary.getId(), beneficiaryBalance);
        return true;
    }

    public Map<Course, Long> getTopPublicCoursesMapToSubsCount(int limit) {
        Map<Course, Long> courseSubsMap = new LinkedHashMap<>();
        courseSubscribeDAO.getTopPublicCoursesIdMapToSubsCount(limit)
                .forEach((k, v) -> courseSubsMap.put(courseDAO.getCourseById(k), v));
        return courseSubsMap;
    }
}
