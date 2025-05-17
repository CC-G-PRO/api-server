package com.cc.demo.entity


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
    val lectureCode: String,
    val isEnglish: Boolean,
    val lecturePlace: String,
    val capacity: Int,
    val syllabusUrl: String,
    val note: String,
    val language: String,
    val createdAt: LocalDateTime,
    var aiDescription: String?,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    val subject: Subject,
    @Column(name = "subject_nm")
    val subjectName: String,


    @OneToMany(mappedBy = "lecture", cascade = [CascadeType.ALL], orphanRemoval = true)
    val times: MutableList<LectureTime> = mutableListOf()
)