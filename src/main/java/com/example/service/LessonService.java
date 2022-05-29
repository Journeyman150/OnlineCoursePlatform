package com.example.service;

import com.example.dao.LessonDAO;
import com.example.domain.Lesson;
import com.example.search_engine.IndexedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public File getFile(Long courseId, int lessonNum) {
        return storageService.load(lessonDAO.getLessonByCourseIdAndLessonNum(courseId, lessonNum).getVideoSource());
    }

    @Transactional
    public void save(Lesson lesson, long courseId, MultipartFile videoFile) {
        lesson.setCourseId(courseId);
        long lessonId = lessonDAO.save(lesson);

        String storagePath = storagePathPrefix + "/" + courseId;
        String videoSource = storagePath + "/" + lesson.getNum() + "_" + videoFile.getOriginalFilename();
        //lesson.setVideoSource(videoSource);

        storageService.store(videoFile, storagePath, videoSource);
        lessonDAO.updateContent(videoSource, courseId, lesson.getNum());
    }

    @Transactional
    public void update(Lesson lesson, long courseId, int prevNum, MultipartFile videoFile) {
        if (!videoFile.isEmpty()) {
            String storagePath = storagePathPrefix + "/" + courseId;
            String videoSource = storagePath + "/" + lesson.getNum() + "_" + videoFile.getOriginalFilename();

            String previousSource = lessonDAO.getLessonByCourseIdAndLessonNum(courseId, prevNum).getVideoSource();
            storageService.delete(previousSource);
            //lesson.setVideoSource(videoSource);
            storageService.store(videoFile, storagePath, videoSource);
            lessonDAO.updateContent(videoSource, courseId, prevNum);
        }
        lessonDAO.updateInfo(lesson, courseId, prevNum);
    }
    @Transactional
    public void delete(long lessonId) {
        String videoSource = lessonDAO.getLessonById(lessonId).getVideoSource();
        storageService.delete(videoSource);
        lessonDAO.delete(lessonId);
    }
    @Transactional
    public void delete(long courseId, int lessonNum) {
        String videoSource = lessonDAO.getLessonByCourseIdAndLessonNum(courseId, lessonNum).getVideoSource();
        storageService.delete(videoSource);
        lessonDAO.delete(courseId, lessonNum);
    }

    public List<IndexedData> getSearchDataList() {
        return lessonDAO.getSearchDataList();
    }
}
