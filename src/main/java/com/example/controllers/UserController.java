package com.example.controllers;

import com.example.domain.Course;
import com.example.domain.Lesson;
import com.example.domain.Role;
import com.example.domain.User;
import com.example.service.CourseService;
import com.example.service.CourseSubscribeService;
import com.example.service.LessonService;
import com.example.service.UserService;
import com.example.service.access.AccessControlService;
import com.example.service.access.CourseInvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class UserController {
    @Value("${authorRolePrice}")
    private int authorRolePrice;

    private UserService userService;
    private CourseService courseService;
    private LessonService lessonService;
    private CourseSubscribeService courseSubscribeService;
    private AccessControlService accessControlService;
    private CourseInvitationService courseInvitationService;

    @Autowired
    public UserController(UserService userService,
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

    @GetMapping("/main_page")
    public String getUserMainPage(Model model,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  @RequestParam(name = "free", required = false) boolean free,
                                  @RequestParam(name = "paid", required = false) boolean paid,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(20);
        List<Course> coursesList = courseService.findCourses(keyword);
        List<Course> filteredCoursesList;
        if (!free && !paid) {
            free = true;
            paid = true;
        }
        if (!free) {
            filteredCoursesList = coursesList.stream().filter(n -> n.getPrice() > 0).collect(Collectors.toList());
        } else if (!paid) {
            filteredCoursesList = coursesList.stream().filter(n -> n.getPrice() == 0).collect(Collectors.toList());
        } else {
            filteredCoursesList = coursesList;
        }
        Page<Course> coursesPage = courseService.getPaginated(filteredCoursesList, PageRequest.of(currentPage - 1, pageSize));
        int totalPages = coursesPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        Map<Long, String> authorMap = new HashMap<>();
        for (int i = 0; i < coursesPage.getContent().size(); i++) {
            long authorId = coursesPage.getContent().get(i).getAuthorId();
            User author = userService.getUserById(authorId);
            String authorName = author.getName() + " " + author.getSurname();
            authorMap.put(authorId, authorName);
        }
        try {
            int balance = userService.getUserById(userService.getAuthorizedUser().getId()).getBalance();
            model.addAttribute("balance", balance);
        } catch (Exception e) {
            System.out.println("Not authorized request.");
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("checkedFree", free);
        model.addAttribute("checkedPaid", paid);
        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("authorMap", authorMap);
        if (coursesPage.isEmpty() && keyword != null && !keyword.matches("\s*")) {
            model.addAttribute("noResults", "The search has not given any results.");
        }
        //top 10 courses view
        Map<Course, Long> courseSubsMap = courseSubscribeService.getTopPublicCoursesMapToSubsCount(10);
        Map<Long, String> topCoursesAuthorMap = new HashMap<>();
        courseSubsMap.forEach((k, v) -> {
            User author = userService.getUserById(k.getAuthorId());
            String authorName = author.getName() + " " + author.getSurname();
            topCoursesAuthorMap.put(k.getAuthorId(), authorName);
        });
        model.addAttribute("courseSubsMap", courseSubsMap);
        model.addAttribute("topCoursesAuthorMap", topCoursesAuthorMap);
        //footer
        model.addAttribute("coursesCount", courseService.getAllCoursesCount());
        return "user/main_page";
    }

    @GetMapping("/courses")
    public String getCoursesPage(Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        List<Course> coursesList = courseSubscribeService.getCoursesListBySubscribedUserId(user.getId());
        if (coursesList.isEmpty()) {
            model.addAttribute("message", "You have not subscribed to any course yet.");
        }
        Map<Long, String> authorMap = new HashMap<>();
        for (int i = 0; i < coursesList.size(); i++) {
            long authorId = coursesList.get(i).getAuthorId();
            User author = userService.getUserById(authorId);
            String authorName = author.getName() + " " + author.getSurname();
            authorMap.put(authorId, authorName);
        }
        model.addAttribute("coursesList", coursesList);
        model.addAttribute("authorMap", authorMap);
        model.addAttribute("balance", user.getBalance());
        return "user/courses";
    }

    @GetMapping("/course/{id}")
    public String getCourse(@PathVariable("id") long courseId,
                            Model model) {
        User user = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        if (user != null) {
            boolean userHasAccessToCourse = accessControlService.userHasAccessToCourse(user.getId(), courseId);
            if (course == null || (course.isNonPublic() & !userHasAccessToCourse)) {
                model.addAttribute("errorMessage", "Access denied.");
                return "error/error_page";
            }
            course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
            model.addAttribute("course", course);
            model.addAttribute("userHasAccessToCourse", userHasAccessToCourse);
            model.addAttribute("subscribersNum", courseSubscribeService.getSubscribedUsersNumberByCourseId(courseId));
            model.addAttribute("balance", userService.getUserById(user.getId()).getBalance());
        }
        if (user == null) {
            if (course == null || course.isNonPublic()) {
                model.addAttribute("errorMessage", "Access denied.");
                return "error/error_page";
            }
            course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
            model.addAttribute("course", course);
            model.addAttribute("userHasAccessToCourse", false);
            model.addAttribute("subscribersNum", courseSubscribeService.getSubscribedUsersNumberByCourseId(courseId));
        }
        return "user/course";
    }
    @PostMapping("/course/subscribe")
    public String subscribeToCourse(@RequestParam("courseId") long courseId,
                                    Model model) {
        Course course = courseService.getPublicCourseById(courseId);
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (course == null || course.isNonPublic()) {
            return "error/error_page";
        }
        if (accessControlService.userHasAccessToCourse(user.getId(), course.getId())) {
            return getCourse(courseId, model);
        }
        User author = userService.getUserById(course.getAuthorId());
        if (user.getId() == author.getId()) {
            courseSubscribeService.subscribe(courseId, user.getId());
            return "redirect:/course/" + courseId;
        }
        if (user.getBalance() < course.getPrice()) {
            model.addAttribute("transactionResults", "Not enough funds on your balance.");
            boolean userHasAccessToCourse = accessControlService.userHasAccessToCourse(user.getId(), courseId);
            course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
            model.addAttribute("course", course);
            model.addAttribute("userHasAccessToCourse", userHasAccessToCourse);
            return "user/course";
        } else {
            courseSubscribeService.subscribeUserAndMakePayment(user, author, course);
        }
        return "redirect:/course/" + courseId;
    }
    @GetMapping("/course/{courseId}/lesson/{num}")
    public String getLesson(@PathVariable("courseId") long courseId,
                            @PathVariable("num") int num,
                            Model model) {
        User user = userService.getAuthorizedUser();
        if (!accessControlService.userHasAccessToCourse(user.getId(), courseId)) {
            model.addAttribute("errorMessage",
                    "You need subscribe to the course before watching the lessons.");
            return getCourse(courseId, model);
        }
        Lesson lesson = lessonService.getLessonByCourseIdAndLessonNum(courseId, num);
        if (lesson == null) {
            model.addAttribute("errorMessage", "Lesson doesn't exist.");
            return "error/error_page";
        }
        Course course = courseService.getCourseById(courseId);
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        model.addAttribute("course", course);
        model.addAttribute("lesson", lesson);
        return "user/lesson";
    }
    @GetMapping("/course/{courseId}/icon")
    public ResponseEntity<Resource> getCourseIcon(@PathVariable long courseId) throws AccessDeniedException {
        User user = userService.getAuthorizedUser();
        if (user == null && courseService.getCourseById(courseId).isNonPublic()) {
            throw new AccessDeniedException("Access to file denied.");
        }
        String iconSource = courseService.getCourseById(courseId).getIconSource();
        Resource resource = new FileSystemResource(courseService.getCourseIcon(iconSource));
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_MIXED)
                .body(resource);
    }
    @GetMapping("/course/{courseId}/demo_video")
    public ResponseEntity<Resource> getDemoVideo(@PathVariable long courseId) throws AccessDeniedException {
        User user = userService.getAuthorizedUser();
        if (user == null && courseService.getCourseById(courseId).isNonPublic()) {
            throw new AccessDeniedException("Access to file denied.");
        }
        List<Lesson> lessons = lessonService.getLessonsListByCourseId(courseId);
        if (lessons.isEmpty()) {
            return null;
        }
        int num = lessons.stream().mapToInt(Lesson::getNum).min().orElse(0);
        Resource resource = new FileSystemResource(lessonService.getFile(courseId, num));
        return ResponseEntity.ok()
                .contentLength(2L*1024*1024*1024)
                .contentType(MediaType.MULTIPART_MIXED)
                .body(resource);
    }
    @GetMapping("/course/{courseId}/lesson/{num}/video")
    public ResponseEntity<Resource> getLessonVideo(@PathVariable long courseId,
                                                   @PathVariable int num) throws IOException {
        User user = userService.getAuthorizedUser();
        if (!accessControlService.userHasAccessToCourse(user.getId(), courseId)) {
            throw new AccessDeniedException("Access to file denied.");
        }
        Resource resource = new FileSystemResource(lessonService.getFile(courseId, num));
        return ResponseEntity.ok()
                .contentLength(10L*1024*1024*1024)
                .contentType(MediaType.MULTIPART_MIXED)
                .body(resource);
    }
    @GetMapping("/invitations")
    public String getInvitations(Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        List<Long> coursesIdList = courseInvitationService.getCoursesIdListByInvitedUserId(user.getId());
        List<Course> coursesList = new ArrayList<>();
        if (coursesIdList.isEmpty()) {
            model.addAttribute("message", "You have no active course invitations.");
        } else {
            coursesIdList.forEach(n -> coursesList.add(courseService.getCourseById(n)));
        }
        Map<Long, String> authorMap = new HashMap<>();
        coursesList.forEach(n -> {
            User author = userService.getUserById(n.getAuthorId());
            authorMap.put(n.getAuthorId(), author.getName() + " " + author.getSurname());
        });
        List<Long> subscribedCoursesIdList = courseSubscribeService.getCoursesIdListByUserId(user.getId());
        Set<Long> subscribedCoursesIdSet = new HashSet<>(subscribedCoursesIdList);
        model.addAttribute("authorMap", authorMap);
        model.addAttribute("subscribedCoursesIdSet", subscribedCoursesIdSet);
        model.addAttribute("coursesList", coursesList);
        model.addAttribute("balance", user.getBalance());
        return "user/invitations";
    }

    @PostMapping("/accept_invitation")
    public String acceptInvitation(@RequestParam("courseId") long courseId) throws Exception {
        User user = userService.getAuthorizedUser();
        List<Long> coursesIdList = courseInvitationService.getCoursesIdListByInvitedUserId(user.getId());
        if (!coursesIdList.contains(courseId)) {
            throw new Exception("User with id " + user.getId() + " does not have invitation for course with id " + courseId);
        }
        courseSubscribeService.subscribe(courseId, user.getId());
        return "redirect:/invitations";
    }

    @GetMapping("/become_a_teacher")
    public String getBecomeATeacherPage(Model model) {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        model.addAttribute("balance", user.getBalance());
        return "user/become_a_teacher";
    }

    @PostMapping("/become_a_teacher")
    public String becomeATeacher(Model model,
                                 HttpServletRequest request) throws ServletException {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (user.getBalance() < authorRolePrice) {
            model.addAttribute("transactionResults", "Not enough funds on your balance.");
            return getBecomeATeacherPage(model);
        }
        userService.changeRole(user.getId(), Role.AUTHOR.name());
        userService.updateUserBalance(user.getId(), user.getBalance() - authorRolePrice);
        request.logout();
        return "redirect:/main_page";
    }
}
