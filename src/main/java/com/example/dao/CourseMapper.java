package com.example.dao;

import com.example.domain.Course;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class CourseMapper implements RowMapper<Course> {
    @Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException {
        Course course = new Course();
        course.setId(rs.getLong("course_id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));
        course.setIconSource(rs.getString("icon_source"));
        course.setAuthorId(rs.getLong("author_id"));
        course.setPrice(rs.getInt("price"));
        course.setNonPublic(rs.getBoolean("non_public"));
        return course;
    }
//    public String getSQLUpdateStringPattern(Course course) throws Exception {
//        StringBuilder SQL = new StringBuilder("UPDATE courses SET ");
//        for (int i = 0; i < Mapper.columns.length; i++) {
//            SQL.append(Mapper.columns[i]).append("=").append(Mapper.getCourseValue(course, i)).append(",");
//        }
//        return SQL.substring(0, SQL.length()-2) + " ";
//    }
//    public String getSQLAddCourseString(Course course) throws Exception {
//        StringBuilder columnsSQL = new StringBuilder("(");
//        StringBuilder valuesSQL = new StringBuilder(") VALUES(");
//        for (int i = 0; i < Mapper.columns.length; i++) {
//            columnsSQL.append(Mapper.columns[i]).append(",");
//            valuesSQL.append(Mapper.getCourseValue(course, i)).append(",");
//        }
//        return "INSERT INTO courses" +
//                columnsSQL.substring(0, columnsSQL.length()-1) +
//                valuesSQL.substring(0, valuesSQL.length()-1) + ")";
//    }
//
//    static class Mapper {
//        private static String[] columns = new String[] {
//                "title", //columnIdx == 0
//                "description", //columnIdx == 1
//                "author_id", //columnIdx == 2
//                "price", //columnIdx == 3
//                "non_public" //columnIdx == 4
//        };
//        private static String getCourseValue(Course course, int columnIdx) throws Exception {
//            if (columnIdx == 0) return String.valueOf(course.getTitle());
//            if (columnIdx == 1) return String.valueOf(course.getDescription());
//            if (columnIdx == 2) return String.valueOf(course.getAuthorId());
//            if (columnIdx == 3) return String.valueOf(course.getPrice());
//            if (columnIdx == 4) return String.valueOf(course.isNonPublic());
//            throw new Exception("Error in CourseMapper.Mapper.class: column index is not mapped.");
//        }
//    }
}
