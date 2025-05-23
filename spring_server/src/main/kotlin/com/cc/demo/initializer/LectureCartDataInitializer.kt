package com.cc.demo.initializer

import com.cc.demo.entity.LectureCart
import com.cc.demo.repository.LectureCartRepository
import com.cc.demo.repository.LectureRepository
import com.cc.demo.repository.UserRepository
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Order(3)
@Component
class LectureCartDataInitializer (
    private val userRepository: UserRepository,
    private val lectureRepository: LectureRepository,
    private val lectureCartRepository: LectureCartRepository

): CommandLineRunner
{
    private val log = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        val user = userRepository.findById(1L).orElse(null)
        if (user == null) {
            log.warn { "should init first user mock data." }
            return
        }

        val lectures = lectureRepository.findAllById((1L..10L).toList())
        if (lectures.size < 10) {
            log.warn { "Lectures with IDs 1 to 15 not found. Found only ${lectures.size}. Skipping insertion." }
            return
        }

        val lectureCarts = lectures.mapIndexed { index, lecture -> //lecture 1~10 까지만 담
            LectureCart(
                user = user,
                priority = index + 1,
                lecture = lecture,
                addedAt = LocalDateTime.now().minusDays(index.toLong())
            )
        }

        lectureCartRepository.saveAll(lectureCarts)
        log.info { "Inserted ${lectureCarts.size} lecture carts for user ID 1 !!!" }
    }
}