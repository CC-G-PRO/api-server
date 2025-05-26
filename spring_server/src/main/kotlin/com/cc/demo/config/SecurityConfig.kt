package com.cc.demo.config

import com.cc.demo.security.JwtAuthenticationFilter

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    private val log = KotlinLogging.logger {}

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        log.info { "Configuring security filter chain" }

        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

            .authorizeHttpRequests {
                it
                    .requestMatchers("/auth/**",
                        "/h2-console/**",
                        ).permitAll()
                  //  .requestMatchers("/actuator/health", "/actuator/info").permitAll() // for health check
                  //  .anyRequest().authenticated()   // 실제로 이거 써야함
                    .anyRequest().permitAll() // 테스트용 (인증 불요)
            }

            .headers { it.frameOptions { it.sameOrigin() } }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        log.info { "Security filter chain configured successfully" }

        return http.build()
    }
}