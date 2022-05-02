package com.example.service;

import com.example.dao.LessonDAO;
import com.example.domain.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@Service
public class LessonService {
    private final LessonDAO lessonDAO;
    private final StorageService storageService;
    @Value("${storage.root.path}")
    private String storagePathPrefix;

    @Autowired
    public LessonService(LessonDAO lessonDAO, StorageService storageService) {
        this.lessonDAO = lessonDAO;
        this.storageService = storageService;
    }

    public List<Lesson> getLessonsListByCourseId(long courseId) {
        return lessonDAO.getLessonsListByCourseId(courseId);
    }

    public Lesson getLessonById(long lessonId) {
        return lessonDAO.getLessonById(lessonId);
    }

    public Lesson getLessonByCourseIdAndLessonNum(long courseId, int lessonNum) {
        return lessonDAO.getLessonByCourseIdAndLessonNum(courseId, lessonNum);
    }

    public boolean isLessonNumExistInCourse(int num, long courseId) {
        if (getLessonsListByCourseId(courseId).stream().filter(n -> n.getNum() == num).findAny().orElse(null) == null) {
            return false;
        } else return true;
    }

    public void save(Lesson lesson, long courseId, MultipartFile videoFile) {
        String storagePath = storagePathPrefix + "/" + courseId;
        String videoSource = storagePath + "/" + lesson.getNum() + "_" + videoFile.getOriginalFilename();
        lesson.setVideoSource(videoSource);
        storageService.store(videoFile, storagePath, videoSource);
        lesson.setCourseId(courseId);
        lessonDAO.save(lesson);
    }

    public void update(Lesson lesson, long courseId, int prevNum, MultipartFile videoFile) {
        if (!videoFile.isEmpty()) {
            String storagePath = storagePathPrefix + "/" + courseId;
            String videoSource = storagePath + "/" + lesson.getNum() + "_" + videoFile.getOriginalFilename();
            lesson.setVideoSource(videoSource);
            storageService.store(videoFile, storagePath, videoSource);
            lessonDAO.updateContent(lesson, courseId, prevNum);
        }
        lessonDAO.updateInfo(lesson, courseId, prevNum);
    }

    public File getFile(Long courseId, int lessonNum) {
        return storageService.load(lessonDAO.getLessonByCourseIdAndLessonNum(courseId, lessonNum).getVideoSource());
    }
}
