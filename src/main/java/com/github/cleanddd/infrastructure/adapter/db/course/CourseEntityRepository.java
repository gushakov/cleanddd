package com.github.cleanddd.infrastructure.adapter.db.course;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEntityRepository extends JpaRepository<CourseEntity, Integer> {

    boolean existsCourseEntityByTitleLike(String title);

}
