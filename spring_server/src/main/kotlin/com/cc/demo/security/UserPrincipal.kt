package com.cc.demo.security

import com.cc.demo.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    val email: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    companion object {
        fun fromUser(user: User): UserPrincipal {
            return UserPrincipal(
                id = user.id,
                email = user.email,
                authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
            )
        }
    }

    override fun getUsername() = email
    override fun getPassword() = null  // Google OAuth2는 비밀번호 없음
    override fun getAuthorities() = authorities
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}
