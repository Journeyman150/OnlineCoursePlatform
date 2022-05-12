package com.example.service;

import com.example.dao.CourseSubscribeDAO;
import com.example.dao.UserDAO;
import com.example.domain.Course;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CourseSubscribeService {
    private UserDAO userDAO;
    private CourseSubscribeDAO courseSubscribeDAO;
    @Autowired
    public CourseSubscribeService(UserDAO userDAO, CourseSubscribeDAO courseSubscribeDAO) {
        this.userDAO = userDAO;
        this.courseSubscribeDAO = courseSubscribeDAO;
    }

    @Transactional
    public void subscribeUserAndMakePayment(User user, User author, Course course) {
        this.makePayment(user, author, course.getPrice());
        this.subscribe(course.getId(), user.getId());

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

    public boolean makePayment(User payer, User beneficiary, int price) {
        int payerBalance = payer.getBalance() - price;
        int beneficiaryBalance = beneficiary.getBalance() + price;
        userDAO.updateUserBalance(payer.getId(), payerBalance);
        userDAO.updateUserBalance(beneficiary.getId(), beneficiaryBalance);
        return true;
    }
}
