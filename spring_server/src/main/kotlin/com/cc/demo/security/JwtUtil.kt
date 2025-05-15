package com.cc.demo.security

import com.cc.demo.repository.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

private val log = KotlinLogging.logger {}

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
    private val userRepository: UserRepository
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: Long, email: String): String {
        val now = Date()
        val expiry = Date(now.time + expiration)

        log.info { "JWT 토큰 생성 - 사용자 이메일: $email, ID: $userId" }

        return Jwts.builder()
            .setSubject(email)
            .claim("userId", userId)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val userId = extractUserId(token)
        log.info { "✅ JWT에서 사용자 ID 추출: $userId" }

        val user = userRepository.findById(userId).orElseThrow {
            log.warn { "해당 ID의 사용자를 찾을 수 없습니다: $userId" }
            IllegalArgumentException("User not found with id $userId")
        }

        val principal = UserPrincipal.fromUser(user)
        return UsernamePasswordAuthenticationToken(principal, null, principal.authorities)
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            log.info { "유효한 JWT 토큰입니다." }
            true
        } catch (e: Exception) {
            log.warn { "JWT 토큰 유효성 검사 실패: ${e.message}" }
            false
        }
    }

    fun extractUserId(token: String): Long {
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .get("userId", Integer::class.java)
            .toLong()
    }
}
