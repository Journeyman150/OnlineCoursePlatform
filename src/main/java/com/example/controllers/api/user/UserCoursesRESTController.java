package com.example.controllers.api.user;

import com.example.domain.Course;
import com.example.domain.User;
import com.example.exception_handling.api.AccessToCourseDeniedException;
import com.example.exception_handling.api.NoSuchCourseException;
import com.example.service.CourseService;
import com.example.service.CourseSubscribeService;
import com.example.service.LessonService;
import com.example.service.UserService;
import com.example.service.access.AccessControlService;
import com.example.service.access.CourseInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/courses")
public class UserCoursesRESTController {

    private UserService userService;
    private CourseService courseService;
    private LessonService lessonService;
    private CourseSubscribeService courseSubscribeService;
    private AccessControlService accessControlService;
    private CourseInvitationService courseInvitationService;

    @Autowired
    public UserCoursesRESTController(UserService userService,
                                     CourseService courseService,
                                     LessonService lessonService,
                                     CourseSubscribeService courseSubscribeService,
                                     AccessControlService accessControlService,
                                     CourseInvitationService courseInvitationService) {
        this.userService = userService;
        this.courseService = courseService;
        this.lessonService = lessonService;
        this.courseSubscribeService = courseSubscribeService;
        this.accessControlService = accessControlService;
        this.courseInvitationService = courseInvitationService;
    }

    @GetMapping()
    public List<Course> getCoursesListByKeyword(@RequestParam(value = "keyword", required = false) String keyword) {
        List<Course> coursesList = courseService.findCourses(keyword);
        if (keyword == null || keyword.matches("\s*")) {
            throw new RuntimeException("Required request parameter 'keyword' for method parameter type String is not present. " +
                    "Request examples: .../api/user/courses?keyword=course; " +
                    "or .../api/user/courses?keyword=java%20core (for multiple keywords)");
        }
        if (coursesList.isEmpty()) {
            throw new NoSuchCourseException("No courses by entered keyword(s) found. To find most of them use ?keyword=course. " +
                    "For multiple keywords use this pattern: ?keyword=course%20spring");
        }
        return coursesList;
    }

    @GetMapping("{courseId}")
    public Course getCourseById(@PathVariable("courseId") long courseId) {
        Course course = courseService.getCourseById(courseId);
        if (course == null) {
            throw new NoSuchCourseException("Course with id " + courseId + " not found");
        }
        long userId = userService.getAuthorizedUser().getId();
        if (course.isNonPublic() && !accessControlService.userHasAccessToCourse(userId, courseId)) {
            throw new AccessToCourseDeniedException(
                    "User with id " + userId + " has no access to non public course with id " + courseId);
        }
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        return course;
    }

    @GetMapping("/subscribed")
    public List<Course> getSubscribedCoursesList() {
        long userId = userService.getAuthorizedUser().getId();
        List<Course> coursesList = courseSubscribeService.getCoursesListBySubscribedUserId(userId);
        if (coursesList.isEmpty()) {
            throw new NoSuchCourseException("User with id " + userId + " is not subscribed to any course");
        }
        return coursesList;
    }

    @PostMapping("/{courseId}/subscribe")
    public Course subscribe(@PathVariable("courseId") long courseId) {
        Course course = courseService.getCourseById(courseId);
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (course == null) {
            throw new NoSuchCourseException("Course with id " + courseId + "doesn't exist");
        }
        if (accessControlService.userHasAccessToCourse(user.getId(), courseId)) {
            throw new AccessToCourseDeniedException(
                    "User with id " + user.getId() + " is already subscribed to course with id " + courseId);
        }
        if (course.isNonPublic()) {
            throw new AccessToCourseDeniedException(
                    "User with id " + user.getId() + " can't subscribe to course with id " + courseId);
        }
        User author = userService.getUserById(course.getAuthorId());
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        if (author.getId() == user.getId()) {
            courseSubscribeService.subscribe(courseId, user.getId());
            return course;
        }
        if (user.getBalance() < course.getPrice()) {
            throw new AccessToCourseDeniedException(
                    "User with id " + user.getId() + " has not enough funds to buy course with id " + courseId);
        }
        courseSubscribeService.subscribeUserAndMakePayment(user, author, course);
        return course;
    }

    @GetMapping("/invitations")
    public List<Course> getInvitedCoursesList() {
        long userId = userService.getAuthorizedUser().getId();
        List<Course> coursesList = courseInvitationService.getCoursesListByInvitedUserId(userId);
        if (coursesList.isEmpty()) {
            throw new NoSuchCourseException("User with id " + userId + " has no invitations");
        }
        return coursesList;
    }

    @PostMapping("/{courseId}/accept_invitation")
    public Course acceptInvitation(@PathVariable("courseId") long courseId) {
        User user = userService.getAuthorizedUser();
        List<Long> coursesIdList = courseInvitationService.getCoursesIdListByInvitedUserId(user.getId());
        if (!coursesIdList.contains(courseId)) {
            throw new AccessToCourseDeniedException(
                    "User with id " + user.getId() + " does not have invitation for course with id " + courseId);
        }
        courseSubscribeService.subscribe(courseId, user.getId());
        Course course = courseService.getCourseById(courseId);
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        return course;
    }
}
