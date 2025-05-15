package com.cc.demo.service

import com.cc.demo.entity.User
import com.cc.demo.repository.UserRepository
import com.cc.demo.response.AuthResponse
import com.cc.demo.response.UserSummary
import com.cc.demo.security.JwtUtil
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import java.util.Base64.getUrlDecoder

private val log = KotlinLogging.logger {}

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtUtil: JwtUtil,
    private val objectMapper: ObjectMapper
) {
    fun loginWithGoogle(idToken: String): AuthResponse {
        val payload = parseGoogleJwtPayload(idToken)

        val sub = payload["sub"] as? String ?: error("Google 사용자 ID 없음")
        val email = payload["email"] as? String ?: error("이메일 없음")
        val name = payload["name"] as? String ?: "알 수 없음"
        val picture = payload["picture"] as? String

        val user = userRepository.findByProviderId(sub) ?: run {
            /**
             * 유저가 db에 존재하지 않으면 회원가입 됨.
             */
            log.info { "🆕 신규 사용자 회원가입 처리 (Google ID): $sub" }
            userRepository.save(
                User(
                    provider = "google",
                    providerId = sub,
                    email = email,
                    name = name,
                    profileImageUrl = picture,
                    createdAt = LocalDateTime.now()
                )
            )
        }

        log.info { "🔐 로그인 성공 - 사용자: ${user.email}" }

        val accessToken = jwtUtil.generateToken(user.id, user.email)
        return AuthResponse(accessToken, UserSummary(user.id, user.name, user.email))
    }

    private fun parseGoogleJwtPayload(token: String): Map<String, Any> {
        val parts = token.split(".")
        require(parts.size == 3) { "유효하지 않은 JWT 형식" }

        val payloadJson = String(getUrlDecoder().decode(parts[1]))
        return objectMapper.readValue(payloadJson, Map::class.java) as Map<String, Any>
    }
}