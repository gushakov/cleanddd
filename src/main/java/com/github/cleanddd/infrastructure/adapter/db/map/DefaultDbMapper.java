package com.github.cleanddd.infrastructure.adapter.db.map;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.infrastructure.adapter.db.course.CourseEntity;
import com.github.cleanddd.infrastructure.adapter.db.enrollment.EnrollmentRow;
import com.github.cleanddd.infrastructure.adapter.db.student.StudentEntity;
import org.springframework.stereotype.Service;

@Service
public class DefaultDbMapper implements DbMapper {
    @Override
    public Course map(CourseEntity entity) {
        return Course.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .numberOfStudents(entity.getNumberOfStudents())
                .build();
    }

    @Override
    public CourseEntity map(Course model) {
        return CourseEntity.builder()
                .id(model.getId())
                .title(model.getTitle())
                .numberOfStudents(model.getNumberOfStudents())
                .build();
    }

    @Override
    public Student map(StudentEntity entity) {
        return Student.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .coursesIds(entity.getCoursesIds())
                .build();
    }

    @Override
    public StudentEntity map(Student model) {
        return StudentEntity.builder()
                .id(model.getId())
                .fullName(model.getFullName())
                .coursesIds(model.getCoursesIds())
                .build();
    }

    @Override
    public Enrollment map(EnrollmentRow row) {
        return Enrollment.builder()
                .courseId(row.getCourseId())
                .courseTitle(row.getCourseTitle())
                .build();
    }
}
