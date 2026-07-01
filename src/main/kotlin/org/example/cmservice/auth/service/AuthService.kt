package org.example.cmservice.auth.service

import jakarta.transaction.Transactional
import org.example.cmservice.auth.mapper.UserMapper
import org.example.cmservice.auth.repository.UserRepository
import org.example.cmservice.auth.service.dto.AuthDTO
import org.example.cmservice.auth.service.dto.TokenDTO
import org.example.cmservice.auth.service.dto.UserDTO
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
open class AuthService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenService: JwtTokenService,
    private val authenticationManager: AuthenticationManager
) {
    @Transactional
    fun registerUser(user: UserDTO, rawPassword: String): UserDTO {
        // Will only return null if rawPassword is null, but rawPassword is not nullable
        val hashed = passwordEncoder.encode(rawPassword)!!
        val user = userMapper.toEntity(user, hashed)

        val saved = userRepository.save(user)
        return userMapper.toDto(saved)
    }
    fun authenticate(auth: AuthDTO): TokenDTO {
        val token = UsernamePasswordAuthenticationToken(auth.username, auth.password)
        val authentication = authenticationManager.authenticate(token)

        val jwtToken = jwtTokenService.generateToken(authentication)
        val expiresAt = jwtTokenService.extractExpirationTime(jwtToken)

        return TokenDTO(jwtToken, authentication.name, expiresAt)
    }

}