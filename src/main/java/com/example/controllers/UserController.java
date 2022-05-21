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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Controller
@RequestMapping("/user")
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
//    @GetMapping()
//    public String getUserMainPage(@RequestParam(name = "keyword", required = false) String keyword,
//                                  Model model) {
//        if (keyword != null && !keyword.equals("") && !keyword.matches("\s+")) {
//            List<Course> coursesList = new ArrayList<>();
//            Set<Long> set = coursesSearchData.findIndexes(CoursesSearchData.getSeparateKeywords(keyword));
//                System.out.println("In user controller: " + Arrays.toString(set.toArray()));
//            if (set.contains(-1L)) {
//                model.addAttribute("noResults", "The search has not given any results.");
//                return "/user/main_page";
//            }
//            set.forEach(n -> coursesList.add(courseService.getCourseById(n)));
//            model.addAttribute("coursesList", coursesList);
//        }
//        return "user/main_page";
//    }
    @GetMapping()
    public String getUserMainPage(Model model,
                                  @RequestParam(name = "keyword", required = false) String keyword,
                                  @RequestParam("page") Optional<Integer> page,
                                  @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(6);
        Page<Course> coursesPage = courseService.findPaginated(keyword, PageRequest.of(currentPage - 1, pageSize));
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
        model.addAttribute("keyword", keyword);
        model.addAttribute("coursesPage", coursesPage);
        model.addAttribute("authorMap", authorMap);
        if (coursesPage.isEmpty() && keyword != null && !keyword.matches("\s*")) {
            model.addAttribute("noResults", "The search has not given any results.");
        }
        return "user/main_page";
    }

    @GetMapping("/courses")
    public String getCoursesPage(Model model) {
        User user = userService.getAuthorizedUser();
        List<Course> coursesList = courseSubscribeService.getCoursesListBySubscribedUserId(user.getId());
        if (coursesList.isEmpty()) {
            model.addAttribute("message", "You have not subscribed to any course yet.");
            return "user/courses";
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
        return "user/courses";
    }

    @GetMapping("/course/{id}")
    public String getCourse(@PathVariable("id") long courseId,
                            Model model) {
        User user = userService.getAuthorizedUser();
        Course course = courseService.getCourseById(courseId);
        boolean userHasAccessToCourse = accessControlService.userHasAccessToCourse(user.getId(), courseId);
        if (course == null || (course.isNonPublic() & !userHasAccessToCourse)) {
            model.addAttribute("errorMessage", "Access denied.");
            return "error/error_page";
        }
        course.setLessonsList(lessonService.getLessonsListByCourseId(courseId));
        model.addAttribute("course", course);
        model.addAttribute("userHasAccessToCourse", userHasAccessToCourse);
        return "user/course";
    }
    @PostMapping("/course/subscribe")
    public String subscribeToCourse(@RequestParam("courseId") long courseId,
                                    Model model) {
        Course course = courseService.getPublicCourseById(courseId);
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (course == null || accessControlService.userHasAccessToCourse(user.getId(), course.getId())) {
            return "error/error_page";
        }
        User author = userService.getUserById(course.getAuthorId());
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
        return "redirect:/user/course/" + courseId;
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
        model.addAttribute("lesson", lesson);
        return "user/lesson";
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
        User user = userService.getAuthorizedUser();
        List<Long> coursesIdList = courseInvitationService.getCoursesIdListByInvitedUserId(user.getId());
        List<Course> coursesList = new ArrayList<>();
        if (coursesIdList.isEmpty()) {
            model.addAttribute("message", "You have no active course invitations.");
            return "user/invitations";
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
        return "redirect:/user/invitations";
    }

    @GetMapping("/become_a_teacher")
    public String getBecomeATeacherPage() {
        return "user/become_a_teacher";
    }

    @PostMapping("/become_a_teacher")
    public String becomeATeacher(Model model,
                                 HttpServletRequest request) throws ServletException {
        User user = userService.getUserById(userService.getAuthorizedUser().getId());
        if (user.getBalance() < authorRolePrice) {
            model.addAttribute("transactionResults", "Not enough funds on your balance.");
            return "user/become_a_teacher";
        }
        userService.changeRole(user.getId(), Role.AUTHOR.name());
        userService.updateUserBalance(user.getId(), user.getBalance() - authorRolePrice);
        request.logout();
        return "redirect:/user";
    }
}
