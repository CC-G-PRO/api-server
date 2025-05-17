package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "subjects")
data class Subject(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "subject_code", unique = true)
    val subjectCode: String,

    @Column(name = "subject_name")
    val subjectName: String,
    val credit: Int,
    val targetGrade: String,
    val typeNumber : Int,
    @Column(name="subject_year")
    val year: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_main_id")
    val categoryMain: CategoryMain,

    @OneToMany(mappedBy = "subject")
    val lectures: List<Lecture> = listOf(),


    @OneToMany(mappedBy = "subject", cascade = [CascadeType.ALL])
    val breadthGeneralEducationList: List<BreadthGeneralEducation> = listOf()
)