package org.example.cmservice.auth.service.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    val usernameValue: String,
    val passwordValue: String,
    val authoritiesValue: Collection<GrantedAuthority>,
): UserDetails {
    override fun getUsername() = usernameValue
    override fun getPassword() = passwordValue
    override fun getAuthorities() = authoritiesValue

    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}