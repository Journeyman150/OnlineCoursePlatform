package com.example.domain;

import com.example.domain.validation.CheckLessonNum;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class Lesson {
    private Long id;
    private Long courseId;
    @NotBlank(message = "Title should not be empty.")
    @Size(min = 2, max = 30, message = "Title should be between 2 and 30 characters.")
    private String title;
    @NotBlank(message = "Description should not be empty.")
    private String description;

    private String videoSource;

    private int num;

    public Lesson() {}

    public Lesson(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVideoSource() {
        return videoSource;
    }

    public void setVideoSource(String videoSource) {
        this.videoSource = videoSource;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "Lesson{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", videoSource='" + videoSource + '\'' +
                ", num=" + num +
                '}';
    }
}
