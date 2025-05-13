package com.cc.demo.entity

import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "lectures")
data class Lecture(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val year: Int,
    val semester: String,
    val professorName: String,
    val section: String,
    val isEnglish: Boolean,
    val lecturePlace: String,
    val capacity: Int,
    val syllabusUrl: String,
    val note: String,
    val language: String,
    val shortDescription: String? = null,
    val createdAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    val subject: Subject,

    @OneToMany(mappedBy = "lecture")
    val times: List<LectureTime> = listOf()
)