package com.example.dao;

import com.example.domain.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LessonDAO {
    private JdbcTemplate jdbcTemplate;
    private LessonMapper lessonMapper;
    @Autowired
    public LessonDAO(JdbcTemplate jdbcTemplate, LessonMapper lessonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.lessonMapper = lessonMapper;
    }
    @Nullable
    public Lesson getLessonById(long id) {
        return jdbcTemplate.query("SELECT * FROM lessons WHERE lesson_id = ?",
                        lessonMapper, id)
                .stream().findAny().orElse(null);
    }
    @Nullable
    public Lesson getLessonByCourseIdAndLessonNum(long courseId, int lessonNum) {
        return jdbcTemplate.query("SELECT * FROM lessons WHERE course_id = ? AND num = ?",
                        lessonMapper, courseId, lessonNum)
                .stream().findAny().orElse(null);
    }

    public List<Lesson> getLessonsListByCourseId(long courseId) {
        return jdbcTemplate.query("SELECT * FROM lessons WHERE course_id = ? ORDER BY num", lessonMapper, courseId);
    }

    public void save(Lesson lesson) {
        jdbcTemplate.update("INSERT INTO lessons(title, description, course_id, video_source, num) VALUES(?, ?, ?, ?, ?)",
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getCourseId(),
                lesson.getVideoSource(),
                lesson.getNum());
    }
    public void updateInfo(Lesson lesson, long courseId, int prevNum) {
        jdbcTemplate.update("UPDATE lessons SET title=?, description=?, num=? WHERE course_id=? AND num=?",
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getNum(),
                courseId,
                prevNum);
    }
    public void updateContent(Lesson lesson, long courseId, int prevNum) {
        jdbcTemplate.update("UPDATE lessons SET video_source=? WHERE course_id=? AND num=?",
                lesson.getVideoSource(),
                courseId,
                prevNum);
    }
    @Nullable
    public long getCourseIdByLessonId(long lessonId) {
        return jdbcTemplate.query("SELECT course_id FROM lessons WHERE lesson_id = ?",
                (rs, rowNum) -> rs.getLong("course_id"),
                lessonId)
                .stream().findAny().orElse(null);
    }
}
