package com.cc.demo.response.lecture_cart

data class CartItemResponse(
    val lecture_id: Long,
    val item_id: Long,
    val course_name: String,
    val priority: Int
)