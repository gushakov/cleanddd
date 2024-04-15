package com.github.cleanddd.infrastructure.adapter.db.student;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "student")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String fullName;

    @ElementCollection
    @CollectionTable(name = "enrollment",
            joinColumns = {@JoinColumn(name = "student_id")})
    @Column(name = "course_id")
    private Set<Integer> coursesIds;
}
