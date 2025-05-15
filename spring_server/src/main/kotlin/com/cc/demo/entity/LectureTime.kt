package com.cc.demo.entity

import com.cc.demo.enumerate.DayOfWeek
import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "lecture_times")
data class LectureTime(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    val lecture: Lecture,

    /**
     * day가 예약어임
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    val day: DayOfWeek,

    val startTime: LocalTime,
    val endTime: LocalTime
)
