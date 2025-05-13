package com.cc.demo.entity

import jakarta.persistence.*

@Entity
@Table(name = "category_main")
data class CategoryMain(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val typeNumber: Int?,
    val name: String
)
