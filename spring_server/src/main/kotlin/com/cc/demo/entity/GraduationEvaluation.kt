package com.cc.demo.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "graduation_evaluations")
data class GraduationEvaluation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    val studentNumber: String,
    val studentName: String,
    val department: String,
    val leftSemester: Int,
    val evaluationDate: LocalDateTime,

    val totalCreditsEarned: Int,
    val totalCreditsRequired: Int,

    val generalFreeCreditsEarned: Int,
    val generalFreeCreditsRequired: Int,

    val generalBreadthCreditsEarned: Int? = null,
    val generalBreadthCreditsRequired: Int? = null,

    val generalRequiredCreditsEarned: Int,
    val generalRequiredCreditsRequired: Int,

    val majorBasicCreditsEarned: Int, //전공 기초
    val majorBasicCreditsRequired: Int,

    val majorRequiredCreditsEarned: Int, //전공 필수
    val majorRequiredCreditsRequired: Int,

    val majorElectiveCreditsEarned: Int, //전공 선택
    val majorElectiveCreditsRequired: Int,

    val majorIndustryCreditsEarned: Int = 0, //산학 필수
    val majorIndustryCreditsRequired: Int = 0,


    val hasGraduationThesis: Boolean,
    val englishCourseCount: Int,
    val englishCourseRequired: Int
)