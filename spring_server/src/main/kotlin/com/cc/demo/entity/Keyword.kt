package com.cc.demo.entity

import jakarta.persistence.*


@Entity
@Table(name = "keywords")
data class Keyword(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
        val keyword: String
)