package com.example.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Objects;

public class Course {
    private long id;
    private long authorId;

    @NotBlank(message = "Title should not be empty.")
    @Size(min = 2, max = 30, message = "Title should be between 2 and 30 characters.")
    private String title;

    @NotBlank(message = "Description should not be empty.")
    private String description;

    private String iconSource;

    private List<Lesson> lessonsList;

    private boolean nonPublic;

    @NotNull(message = "Enter the price (enter 0 if it free)")
    private int price;

    public Course() {
    }

    public Course(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return id == course.id && authorId == course.authorId && nonPublic == course.nonPublic && price == course.price && Objects.equals(title, course.title) && Objects.equals(description, course.description) && Objects.equals(iconSource, course.iconSource) && Objects.equals(lessonsList, course.lessonsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(long authorId) {
        this.authorId = authorId;
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

    public String getIconSource() {
        return iconSource;
    }

    public void setIconSource(String iconSource) {
        this.iconSource = iconSource;
    }

    public List<Lesson> getLessonsList() {
        return lessonsList;
    }

    public void setLessonsList(List<Lesson> lessonsList) {
        this.lessonsList = lessonsList;
    }

    public boolean isNonPublic() {
        return nonPublic;
    }

    public void setNonPublic(boolean nonPublic) {
        this.nonPublic = nonPublic;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
