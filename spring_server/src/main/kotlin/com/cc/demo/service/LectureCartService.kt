package com.cc.demo.service

import com.cc.demo.entity.LectureCart
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.UserRepository
import com.cc.demo.request.lecture_cart.CartAddRequest
import com.cc.demo.request.lecture_cart.CartUpdateRequest
import com.cc.demo.response.lecture_cart.CartItemResponse
import com.cc.demo.response.lecture_cart.CartResponse
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class  LectureCartService(
    private val cartRepository: LectureCartRepository,
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository
) {
    fun addToCart(userId: Long, request: CartAddRequest): CartResponse {
        val user = userRepository.getReferenceById(userId)
        val lecture = lectureRepository.getReferenceById(request.lecture_id)

        val saved = cartRepository.save(
            LectureCart(
                user = user,
                lecture = lecture,
                priority = request.priority,
                addedAt = LocalDateTime.now()
            )
        )
        val count = cartRepository.findAllByUser(user).size
        return CartResponse("cart item POST 성공", mapOf("cart_item_id" to saved.id, "cart_item_count" to count))
    }

    @Transactional
    fun updateCart(userId: Long, request: CartUpdateRequest): CartResponse {
        val user = userRepository.getReferenceById(userId)
        request.updates.forEach {
            val item = cartRepository.findById(it.item_id).orElseThrow()
            if (item.user.id != user.id) throw IllegalAccessException("권한 없음")
            item.priority = it.priority
        }
        val count = cartRepository.findAllByUser(user).size
        return CartResponse("cart item PATCH 성공", mapOf("cart_item_count" to count))
    }

    @Transactional
    fun deleteCartItem(userId: Long, lectureId: Long) {
        val user = userRepository.getReferenceById(userId)
        cartRepository.deleteByUserAndLectureId(user, lectureId)
    }

    fun getCart(userId: Long): List<CartItemResponse> {
        val user = userRepository.getReferenceById(userId)
        return cartRepository.findAllByUser(user).map {
            CartItemResponse(
                lecture_id = it.lecture.id,
                course_name = it.lecture.subjectName,
                priority = it.priority,
                item_id = it.id
            )
        }
    }
}
