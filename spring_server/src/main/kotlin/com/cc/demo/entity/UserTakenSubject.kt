package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_taken_subjects")
data class UserTakenSubject(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    val subjectCode: String,
    val subjectName: String,
    val enrollYear: Int,
    val enrollSemester: Int, // 1 or 2
    val category: String
)