package org.example.cmservice.common.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import tools.jackson.databind.ObjectMapper

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val objectMapper: ObjectMapper
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder =
        BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable()}
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/auth", "/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling {
                it.authenticationEntryPoint { _: HttpServletRequest, res: HttpServletResponse, _: org.springframework.security.core.AuthenticationException ->
                    res.status = HttpStatus.UNAUTHORIZED.value()
                    res.contentType = MediaType.APPLICATION_JSON_VALUE
                    res.writer.write(objectMapper.writeValueAsString(mapOf("error" to "unauthorized")))
                }
                it.accessDeniedHandler { _: HttpServletRequest, res: HttpServletResponse, _: org.springframework.security.access.AccessDeniedException ->
                    res.status = HttpStatus.FORBIDDEN.value()
                    res.contentType = MediaType.APPLICATION_JSON_VALUE
                    res.writer.write(objectMapper.writeValueAsString(mapOf("error" to "forbidden")))
                }
            }

        return http.build()
    }
}