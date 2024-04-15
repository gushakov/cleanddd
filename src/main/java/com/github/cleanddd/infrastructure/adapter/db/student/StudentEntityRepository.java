package com.github.cleanddd.infrastructure.adapter.db.student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentEntityRepository extends JpaRepository<StudentEntity, Integer> {

    boolean existsByFullNameLike(String fullName);
}
