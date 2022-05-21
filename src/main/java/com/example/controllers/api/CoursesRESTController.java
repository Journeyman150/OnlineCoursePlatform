package com.example.controllers.api;

import com.example.domain.Course;
import com.example.exception_handling.api.IncorrectCourseData;
import com.example.exception_handling.api.NoSuchCourseException;
import com.example.service.CourseService;
import com.example.service.LessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CoursesRESTController {
    private final CourseService courseService;
    private final LessonService lessonService;

    @Autowired
    public CoursesRESTController(CourseService courseService, LessonService lessonService) {
        this.courseService = courseService;
        this.lessonService = lessonService;
    }

    @GetMapping()
    public List<Course> getAllCourses() {
        List<Course> coursesList = courseService.getAllCourses();
        coursesList.parallelStream().forEach(n -> n.setLessonsList(lessonService.getLessonsListByCourseId(n.getId())));
        return coursesList;
    }

    @GetMapping("/{id}")
    public Course getCourse(@PathVariable("id") long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            throw new NoSuchCourseException("Course with id " + id + " not found");
        }
        course.setLessonsList(lessonService.getLessonsListByCourseId(id));
        return course;
    }

    @PostMapping()
    public Course createCourse(@RequestBody Course course) {
        long courseId = courseService.save(course);
        course.setId(courseId);
        return course;
    }

    @PutMapping()
    public Course updateCourse(@RequestBody Course course) {
        if (courseService.getCourseById(course.getId()) == null) {
            throw new NoSuchCourseException("Course with id " + course.getId() + " not found");
        }
        courseService.update(course, course.getId());
        return course;
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(@PathVariable("id") long id) {
        if (courseService.getCourseById(id) == null) {
            throw new NoSuchCourseException("Course with id " + id + " not found");
        }
        courseService.delete(id);
        return "Course with id " + id + " was deleted";
    }
}
