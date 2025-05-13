package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "breadth_general_education")
data class BreadthGeneralEducation(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    val subject: Subject,

    val type: String // 실제 enum으로 바꿔도 됨
)
