package com.cc.demo.initializer

import com.cc.demo.entity.User
import com.cc.demo.repository.UserRepository
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Order(1)
@Component
class UserDataInitializer(
    private val userRepository: UserRepository
) : CommandLineRunner {
    private val log = KotlinLogging.logger {}

    override fun run(vararg args: String?) {
        if (userRepository.count() == 0L) {
            val users = listOf(
                User(
                    provider = "google",
                    providerId = "google-uid-1",
                    email = "user1@example.com",
                    name = "김클컴",
                    profileImageUrl = null,
                    createdAt = LocalDateTime.now()
                ),
                User(
                    provider = "google",
                    providerId = "google-uid-2",
                    email = "user2@example.com",
                    name = "박개발",
                    profileImageUrl = null,
                    createdAt = LocalDateTime.now()
                )
            )
            userRepository.saveAll(users)
            log.info { "✅ 테스트 유저 2명 생성 완료" }
        }
    }
}