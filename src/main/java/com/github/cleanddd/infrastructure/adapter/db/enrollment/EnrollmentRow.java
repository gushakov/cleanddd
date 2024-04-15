package com.github.cleanddd.infrastructure.adapter.db.enrollment;

import lombok.Data;

@Data
public class EnrollmentRow {

    public static final String SQL = "select e.student_id, e.course_id, s.full_name as \"student_full_name\", c.title as \"course_title\" from student s" +
            "   join enrollment e on s.id = e.student_id" +
            "   join course c on c.id = e.course_id" +
            "   where s.id = :studentId;";

    Integer courseId;
    String courseTitle;
    Integer studentId;
    String studentFullName;

}
