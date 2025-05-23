package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "timetable_lectures")
data class TimeTableLecture(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_id")
    val timetable: TimeTable,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    val lecture: Lecture
)
