package com.example.service;

import com.example.dao.CourseDAO;
import com.example.dao.CourseUserSubscribedDAO;
import com.example.dao.UserDAO;
import com.example.domain.Course;
import com.example.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseSubscribingUserService {
    private UserDAO userDAO;
    private CourseDAO courseDAO;
    private CourseUserSubscribedDAO courseUserSubscribedDAO;
    @Autowired
    public CourseSubscribingUserService(UserDAO userDAO, CourseDAO courseDAO, CourseUserSubscribedDAO courseUserSubscribedDAO) {
        this.userDAO = userDAO;
        this.courseDAO = courseDAO;
        this.courseUserSubscribedDAO = courseUserSubscribedDAO;
    }

    @Transactional
    public void subscribeUserAndMakePayment(User user, User author, Course course) {
        this.makePayment(user, author, course.getPrice());
        this.subscribe(course.getId(), user.getId());

    }

    public void subscribe(long courseId, long userId) {
        courseUserSubscribedDAO.subscribe(courseId, userId);
    }

    public boolean makePayment(User payer, User beneficiary, int price) {
        int payerBalance = payer.getBalance() - price;
        int beneficiaryBalance = beneficiary.getBalance() + price;
        userDAO.updateUserBalance(payer.getId(), payerBalance);
        userDAO.updateUserBalance(beneficiary.getId(), beneficiaryBalance);
        return true;
    }
}
