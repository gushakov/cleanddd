package com.github.cleanddd.adapter.jpa;

import com.github.cleanddd.adapter.jpa.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentEntityRepository extends JpaRepository<StudentEntity, Integer> {

    boolean existsByFullNameLike(String fullName);
}
