package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "subjects")
data class Subject(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "subject_code", unique = true)
    val subjectCode: String,

    val subjectName: String,
    val credit: Int,
    val targetGrade: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_main_id")
    val categoryMain: CategoryMain,

    @OneToMany(mappedBy = "subject")
    val lectures: List<Lecture> = listOf(),

    @ManyToMany
    @JoinTable(
        name = "subject_keywords",
        joinColumns = [JoinColumn(name = "subject_id")],
        inverseJoinColumns = [JoinColumn(name = "keyword_id")]
    )
    val keywords: List<Keyword> = listOf()


)