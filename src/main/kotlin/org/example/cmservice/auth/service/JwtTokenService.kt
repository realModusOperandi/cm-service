package org.example.cmservice.auth.service

import org.example.cmservice.auth.service.dto.UserPrincipal
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class JwtTokenService(
    val encoder: JwtEncoder,
    val decoder: JwtDecoder,
) {
    fun generateToken(authentication: Authentication): String {
        val now = Instant.now()
        val scope = "ROLE_ADMIN"
        val principal = authentication.principal as UserPrincipal
        val claims = JwtClaimsSet.builder()
            .issuer("https://self")
            .issuedAt(now)
            .expiresAt(now.plus(1, ChronoUnit.HOURS))
            .subject(principal.username)
            .claim("scope", scope)
            .claim("uid", principal.id)
            .build()

        val encoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)
        return this.encoder.encode(encoderParameters).tokenValue
    }

    fun extractExpirationTime(token: String): Long {
        val jwt = decoder.decode(token)
        val exp = jwt.getClaim<Instant>("exp")!!
        return exp.toEpochMilli()
    }
}