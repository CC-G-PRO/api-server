package com.cc.demo.entity

import com.cc.demo.enumerate.Category
import jakarta.persistence.*

@Entity
@Table(name = "subjects")
data class Subject(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "subject_code", unique = true)
    val subjectCode: String,

    val credit: Int,

    @Column(name = "target_grade")
    val targetGrade: String,

    @Column(name = "type_number")
    val typeNumber : Int, //이수구분

    @Column(name = "ai_description")
    var aiDescription: String?,

    var category: Category,


    @OneToMany(mappedBy = "subject")
    val lectures: List<Lecture> = listOf()

)