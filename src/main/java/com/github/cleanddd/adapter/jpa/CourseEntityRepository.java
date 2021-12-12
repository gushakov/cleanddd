package com.github.cleanddd.adapter.jpa;

import com.github.cleanddd.adapter.jpa.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEntityRepository extends JpaRepository<CourseEntity, Integer> {

    boolean existsCourseEntityByTitleLike(String title);
}
