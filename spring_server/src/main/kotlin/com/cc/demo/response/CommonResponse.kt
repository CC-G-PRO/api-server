package com.cc.demo.response

data class CommonResponse(
    val valid: Boolean,
    val message: String,
    val data : Any? = null
)

