package com.cc.demo.request.lecture_cart

data class CartUpdateRequest(
    val updates: List<CartItemUpdate>
)

data class CartItemUpdate(
    val item_id: Long,
    val priority: Int
)