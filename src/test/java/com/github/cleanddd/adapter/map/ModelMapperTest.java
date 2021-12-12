package com.github.cleanddd.adapter.map;

import com.github.cleanddd.adapter.jpa.entity.CourseEntity;
import com.github.cleanddd.adapter.jpa.entity.StudentEntity;
import com.github.cleanddd.model.Course;
import com.github.cleanddd.model.Student;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class ModelMapperTest {

    @Test
    void testMapCourseToCourseEntity() {

        final ModelMapper mapper = new DefaultModelMapper();

        final Course course = Course.builder()
                .id(1)
                .title("English Composition 101")
                .build();

        Assertions.assertThat(course.getNumberOfStudents())
                .isEqualTo(0);

        final CourseEntity entity = mapper.map(course);

        Assertions.assertThat(entity)
                .extracting(CourseEntity::getId, CourseEntity::getTitle, CourseEntity::getNumberOfStudents)
                .containsOnly(1, "English Composition 101", 0);

    }

    @Test
    void testMapCourseEntityToModel() {

        final ModelMapper mapper = new DefaultModelMapper();

        final CourseEntity entity = new CourseEntity();

        entity.setId(1);
        entity.setTitle("English Composition 101");

        final Course course = mapper.map(entity);

        Assertions.assertThat(course)
                .extracting(Course::getId, Course::getTitle)
                .containsOnly(1, "English Composition 101");

    }

    @Test
    void testMapStudentEntityToModel_NullCoursesIdsSetMapsToEmptySet() {
        final ModelMapper mapper = new DefaultModelMapper();

        final StudentEntity entity = new StudentEntity();
        entity.setId(1);
        entity.setFullName("Brad Pitt");
        entity.setCoursesIds(null);

        final Student student = mapper.map(entity);

        Assertions.assertThat(student.getCoursesIds())
                .isNotNull()
                .isEmpty();

    }

    @Test
    void testMapStudentEntityToModel_SameCoursesIdsSet() {
        final ModelMapper mapper = new DefaultModelMapper();

        final StudentEntity entity = new StudentEntity();
        entity.setId(1);
        entity.setFullName("Brad Pitt");
        entity.setCoursesIds(Set.of(1, 2, 3));

        final Student student = mapper.map(entity);

        Assertions.assertThat(student.getCoursesIds())
                .containsOnly(1, 2, 3);

    }
}
