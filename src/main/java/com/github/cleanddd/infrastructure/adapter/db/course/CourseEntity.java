package com.github.cleanddd.infrastructure.adapter.db.course;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "course")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column
    private String title;

    @Column
    private Integer numberOfStudents;

}
