package com.cc.demo.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "lecture_cart")
data class LectureCart(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    val priority: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    val lecture: Lecture,

    val addedAt: LocalDateTime
)