package com.cc.demo.controller

import com.cc.demo.request.lecture_cart.CartAddRequest
import com.cc.demo.request.lecture_cart.CartUpdateRequest
import com.cc.demo.response.lecture_cart.CartItemResponse
import com.cc.demo.response.lecture_cart.CartResponse
import com.cc.demo.security.UserPrincipal
import com.cc.demo.service.LectureCartService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/carts")
class LectureCartController(
    private val cartService: LectureCartService
) {
    @PostMapping
    fun addCart(
        @RequestBody request: CartAddRequest,
        @AuthenticationPrincipal user: UserPrincipal,
    ): CartResponse {
        return cartService.addToCart(user.id, request)
    }

    @PatchMapping
    fun updateCart(
        @RequestBody request: CartUpdateRequest,
        @AuthenticationPrincipal user: UserPrincipal,
    ): CartResponse {
        return cartService.updateCart(user.id, request)
    }

    @DeleteMapping("/{lectureId}")
    fun deleteCartItem(
        @PathVariable lectureId: Long,
        @AuthenticationPrincipal user: UserPrincipal,
    ): Map<String, String> {
        cartService.deleteCartItem(user.id, lectureId)
        return mapOf("message" to "삭제 완료")
    }

    @GetMapping
    fun getCart(
        @AuthenticationPrincipal user: UserPrincipal,
    ): List<CartItemResponse> {
        return cartService.getCart(user.id)
    }
}
