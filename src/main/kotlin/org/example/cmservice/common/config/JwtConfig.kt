package org.example.cmservice.common.config

import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import javax.crypto.spec.SecretKeySpec

@Configuration
class JwtConfig {
    @Value("\${jwt.key}")
    var jwtKey: String? = null

    @Bean
    fun jwtEncoder(): JwtEncoder =
        NimbusJwtEncoder(ImmutableSecret(jwtKey!!.toByteArray()))

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val bytes = jwtKey!!.toByteArray()
        val originalKey = SecretKeySpec(bytes, 0, bytes.size, "RSA")
        return NimbusJwtDecoder.withSecretKey(originalKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }
}