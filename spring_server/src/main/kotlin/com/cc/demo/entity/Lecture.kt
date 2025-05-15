package com.cc.demo.entity

import com.cc.demo.response.CourseResponse
import com.cc.demo.response.LectureTimeResponse
import jakarta.persistence.*
import java.time.LocalDateTime


@Entity
@Table(name = "lectures")
data class Lecture(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /**
     * year이 h2 예약어
     */
    @Column(name="lecture_year")
    val year: Int,
    val semester: Int,
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


    @OneToMany(mappedBy = "lecture", cascade = [CascadeType.ALL], orphanRemoval = true)
    val times: MutableList<LectureTime> = mutableListOf()
)