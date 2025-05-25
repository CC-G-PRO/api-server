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
    val year: Int, //개설 년도

    val semester: Int, //개설 학기

    @Column(name = "professor_name")
    val professorName: String,

    @Column(name = "subject_name")
    val subjectName: String,

    @Column(name = "division_code")
    val divisionCode: String, //분반

    @Column(name = "is_english")
    val isEnglish: Boolean,

    @Column(name = "lecture_place")
    val lecturePlace: String,

    val capacity: Int,

    @Column(name = "syllabus_url")
    val syllabusUrl: String,

    val note: String,

    val language: String,

    @Column(name = "created_at")
    val createdAt: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    val subject: Subject,

    @OneToMany(mappedBy = "lecture", cascade = [CascadeType.ALL], orphanRemoval = true)
    val times: MutableList<LectureTime> = mutableListOf()
)