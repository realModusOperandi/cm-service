package org.example.cmservice.common.util

import org.example.cmservice.auth.service.dto.UserPrincipal
import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt

class JwtUserPrincipalConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val id = (jwt.claims["uid"] as Number).toLong()
        val username = jwt.subject!!

        val authorities: Collection<GrantedAuthority> =
            (jwt.getClaimAsString("scope") ?: "")
                .split(" ")
                .filter { it.isNotBlank() }
                .map { SimpleGrantedAuthority(it) }

        val principal = UserPrincipal(
            id = id,
            usernameValue = username,
            passwordValue = "",
            authoritiesValue = authorities
        )

        return UsernamePasswordAuthenticationToken(principal, "n/a", authorities)
    }
}