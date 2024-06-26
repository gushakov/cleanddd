package com.github.cleanddd.infrastructure.adapter.db;

import com.github.cleanddd.core.model.course.Course;
import com.github.cleanddd.core.model.enrollment.Enrollment;
import com.github.cleanddd.core.model.student.Student;
import com.github.cleanddd.core.port.db.EntityDoesNotExistError;
import com.github.cleanddd.core.port.db.PersistenceError;
import com.github.cleanddd.core.port.db.PersistenceOperationsOutputPort;
import com.github.cleanddd.infrastructure.adapter.db.course.CourseEntityRepository;
import com.github.cleanddd.infrastructure.adapter.db.enrollment.EnrollmentRow;
import com.github.cleanddd.infrastructure.adapter.db.map.DbMapper;
import com.github.cleanddd.infrastructure.adapter.db.student.StudentEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersistenceGateway implements PersistenceOperationsOutputPort {

    final CourseEntityRepository courseRepo;
    final StudentEntityRepository studentRepo;
    final NamedParameterJdbcOperations jdbcOps;
    final DbMapper dbMapper;

    @Transactional
    @Override
    public Integer persist(Course course) {
        try {
            return courseRepo.save(dbMapper.map(course)).getId();
        } catch (Exception e) {
            throw new PersistenceError("Error saving course with ID: %d".formatted(course.getId()), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Course obtainCourseById(Integer courseId) {
        try {
            return dbMapper.map(courseRepo.getById(courseId));
        } catch (EntityNotFoundException e) {
            throw new EntityDoesNotExistError("Could not find Course with ID: %d".formatted(courseId));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean courseExistsWithTitle(String title) {
        try {
            return courseRepo.existsCourseEntityByTitleLike(title);
        } catch (Exception e) {
            throw new PersistenceError("Could not query for course with title matching: \"%s\""
                    .formatted(title), e);
        }
    }

    @Transactional
    @Override
    public Integer persist(Student student) {
        try {
            return studentRepo.save(dbMapper.map(student)).getId();
        } catch (Exception e) {
            throw new PersistenceError("Error saving student with ID: %d".formatted(student.getId()), e);
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Student obtainStudentById(Integer studentId) {
        try {
            return dbMapper.map(studentRepo.getById(studentId));
        } catch (EntityNotFoundException e) {
            throw new EntityDoesNotExistError("Could not find Student with ID: %d".formatted(studentId));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public boolean studentExistsWithFullName(String fullName) {
        try {
            return studentRepo.existsByFullNameLike(fullName);
        } catch (Exception e) {
            throw new PersistenceError("Could not query for student with full name: %s".formatted(fullName));
        }
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Enrollment> findEnrollments(Integer studentId) {

        try {
            return jdbcOps.queryForStream(EnrollmentRow.SQL,
                            Map.of("studentId", studentId),
                            new BeanPropertyRowMapper<>(EnrollmentRow.class))
                    .map(dbMapper::map)
                    .collect(Collectors.toSet());
        } catch (DataAccessException e) {
            throw new PersistenceError("Could not query for enrollments with for student with ID: %d".formatted(studentId));
        }
    }
}
