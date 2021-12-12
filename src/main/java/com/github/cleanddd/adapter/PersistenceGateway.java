package com.github.cleanddd.adapter;

import com.github.cleanddd.adapter.jpa.CourseEntityRepository;
import com.github.cleanddd.adapter.jpa.StudentEntityRepository;
import com.github.cleanddd.adapter.map.ModelMapper;
import com.github.cleanddd.dto.EnrollmentRow;
import com.github.cleanddd.exception.EntityDoesNotExistError;
import com.github.cleanddd.model.Course;
import com.github.cleanddd.model.Enrollment;
import com.github.cleanddd.model.Student;
import com.github.cleanddd.port.PersistenceOperationsOutputPort;
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
    final ModelMapper mapper;

    @Override
    public Integer persist(Course course) {
        return courseRepo.save(mapper.map(course)).getId();
    }

    @Override
    public Course obtainCourseById(Integer courseId) {
        try {
            return mapper.map(courseRepo.getById(courseId));
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
        return studentRepo.save(mapper.map(student)).getId();
    }

    @Override
    public Student obtainStudentById(Integer studentId) {
        try {
            return mapper.map(studentRepo.getById(studentId));
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
                .map(mapper::map)
                .collect(Collectors.toSet());
    }
}
