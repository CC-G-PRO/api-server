package com.cc.demo.response


data class AuthResponse(
    val access_token: String,
    val user: UserSummary
)

data class UserSummary(
    val id: Long,
    val name: String,
    val email: String
)