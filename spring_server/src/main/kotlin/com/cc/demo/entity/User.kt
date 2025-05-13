package com.cc.demo.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val provider: String,
    val providerId: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val createdAt: LocalDateTime
)