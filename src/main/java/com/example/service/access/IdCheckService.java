package com.example.service.access;

import java.util.List;

public interface IdCheckService {

    long getAuthorIdByCourseId(long courseId);

    long getCourseIdByLessonId(long lessonId);

    List<Long> getAccessibleCoursesIdListByUserId(long userId);

}
