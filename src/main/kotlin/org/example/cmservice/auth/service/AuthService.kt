package org.example.cmservice.auth.service

import jakarta.transaction.Transactional
import org.example.cmservice.auth.mapper.UserMapper
import org.example.cmservice.auth.repository.UserRepository
import org.example.cmservice.auth.service.dto.UserDTO
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
open class AuthService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun registerUser(user: UserDTO, rawPassword: String): UserDTO {
        // Will only return null if rawPassword is null, but rawPassword is not nullable
        val hashed = passwordEncoder.encode(rawPassword)!!
        val user = userMapper.toEntity(user, hashed)

        val saved = userRepository.save(user)
        return userMapper.toDto(saved)
    }
}