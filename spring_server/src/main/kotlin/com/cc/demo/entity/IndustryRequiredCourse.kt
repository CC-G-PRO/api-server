package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "industry_required_courses")
data class IndustryRequiredCourse(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    val subject: Subject
)
