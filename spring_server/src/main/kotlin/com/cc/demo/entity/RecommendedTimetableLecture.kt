package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "recommended_timetable_lectures")
data class RecommendedTimetableLecture(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_timetable_id")
    val timetable: RecommendedTimetable,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    val lecture: Lecture
)
