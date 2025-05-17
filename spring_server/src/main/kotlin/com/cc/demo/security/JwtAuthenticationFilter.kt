package com.cc.demo.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            log.debug { "Authorization 헤더에서 JWT 추출 완료" }

            if (jwtUtil.validateToken(token)) {
                val authentication = jwtUtil.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
                log.info { "사용자 인증 완료: ${authentication.name}" }
            } else {
                log.warn { "유효하지 않은 JWT 토큰입니다." }
            }
        } else {
            log.debug { "Authorization 헤더가 없거나 형식이 잘못되었습니다." }
        }

        filterChain.doFilter(request, response)
    }
}
