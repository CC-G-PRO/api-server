package com.cc.demo.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "recommended_timetables")
data class RecommendedTimetable(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    val description: String? = null,
    val createdAt: LocalDateTime
)
