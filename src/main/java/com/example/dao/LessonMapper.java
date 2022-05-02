package com.example.dao;

import com.example.domain.Lesson;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LessonMapper implements RowMapper<Lesson> {
    @Override
    public Lesson mapRow(ResultSet rs, int rowNum) throws SQLException {
        Lesson lesson = new Lesson();
        lesson.setId(rs.getLong("lesson_id"));
        lesson.setTitle(rs.getString("title"));
        lesson.setDescription(rs.getString("description"));
        lesson.setCourseId(rs.getLong("course_id"));
        lesson.setVideoSource(rs.getString("video_source"));
        lesson.setNum(rs.getInt("num"));
        return lesson;
    }
}
