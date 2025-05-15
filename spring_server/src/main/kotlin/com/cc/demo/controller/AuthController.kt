package com.cc.demo.controller

import com.cc.demo.response.AuthResponse
import com.cc.demo.service.AuthService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/google")
    fun loginWithGoogle(@RequestBody body: Map<String, String>): ResponseEntity<AuthResponse> {
        val idToken = body["id_token"] ?: return ResponseEntity.badRequest().build()
        log.info { "🟢 Google 로그인 요청 수신됨" }

        val authResponse = authService.loginWithGoogle(idToken)
        return ResponseEntity.ok(authResponse)
    }
}
