package com.cc.demo.entity

import com.cc.demo.enumerate.TimeTableType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "timetables")
data class TimeTable(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TimeTableType = TimeTableType.GENERATED, //default 로 자동 생성.

    val createdAt: LocalDateTime,
    )