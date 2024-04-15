package com.github.cleanddd.infrastructure.adapter.db;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.core.port.db.EntityDoesNotExistError;
import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;
import com.github.cleanddd.infrastructure.adapter.db.course.CourseEntityRepository;
import com.github.cleanddd.infrastructure.adapter.db.enrollment.EnrollmentRow;
import com.github.cleanddd.infrastructure.adapter.db.map.DbMapper;
import com.github.cleanddd.infrastructure.adapter.db.student.StudentEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersistenceGateway implements PersistenceOperationsOutputPort {

    final CourseEntityRepository courseRepo;
    final StudentEntityRepository studentRepo;
    final NamedParameterJdbcOperations jdbcOps;
    final DbMapper dbMapper;

    @Override
    public Integer persist(Course course) {
        return courseRepo.save(dbMapper.map(course)).getId();
    }

    @Override
    public Course obtainCourseById(Integer courseId) {
        try {
            return dbMapper.map(courseRepo.getById(courseId));
        } catch (EntityNotFoundException e) {
            throw new EntityDoesNotExistError("Could not find Course with ID: %d".formatted(courseId));
        }
    }

    @Override
    public boolean courseExistsWithTitle(String title) {
        return courseRepo.existsCourseEntityByTitleLike(title);
    }

    @Override
    public Integer persist(Student student) {
        return studentRepo.save(dbMapper.map(student)).getId();
    }

    @Override
    public Student obtainStudentById(Integer studentId) {
        try {
            return dbMapper.map(studentRepo.getById(studentId));
        } catch (EntityNotFoundException e) {
            throw new EntityDoesNotExistError("Could not find Student with ID: %d".formatted(studentId));
        }
    }

    @Override
    public boolean studentExistsWithFullName(String fullName) {
        return studentRepo.existsByFullNameLike(fullName);
    }

    @Override
    public Set<Enrollment> findEnrollments(Integer studentId) {

        return jdbcOps.queryForStream(EnrollmentRow.SQL,
                        Map.of("studentId", studentId),
                        new BeanPropertyRowMapper<>(EnrollmentRow.class))
                .map(dbMapper::map)
                .collect(Collectors.toSet());
    }
}
