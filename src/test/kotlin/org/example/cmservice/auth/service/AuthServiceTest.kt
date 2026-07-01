package org.example.cmservice.auth.service

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.cmservice.auth.domain.User
import org.example.cmservice.auth.mapper.UserMapper
import org.example.cmservice.auth.repository.UserRepository
import org.example.cmservice.auth.service.dto.AuthDTO
import org.example.cmservice.auth.service.dto.UserDTO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AuthServiceTest {
    @MockK lateinit var authenticationManager: AuthenticationManager
    @MockK lateinit var jwtTokenService: JwtTokenService
    @MockK lateinit var userRepository: UserRepository
    @MockK lateinit var passwordEncoder: PasswordEncoder
    @MockK lateinit var userMapper: UserMapper

    lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = AuthService(userRepository, userMapper, passwordEncoder, jwtTokenService, authenticationManager)
    }

    @Test
    fun `registerUser encodes password and saves user`() {
        val rawPassword = "plainpass"
        val userDto = UserDTO(username = "newuser", email = "x@email.com")
        val hashed = "encodedpass"
        val userEntity = User(username = "newuser", email = "x@email.com", passwordHash = hashed)
        val savedEntity = User(id = 1L, username = userEntity.username, email = userEntity.email, passwordHash = hashed)
        val savedDto = userDto.copy(id = 1L)

        every { passwordEncoder.encode("plainpass") } returns hashed
        every { userMapper.toEntity(userDto, hashed) } returns userEntity
        every { userRepository.save(userEntity) } returns savedEntity
        every { userMapper.toDto(savedEntity) } returns savedDto

        val result = authService.registerUser(userDto, rawPassword)

        assertEquals(savedDto, result)

        verifySequence {
            passwordEncoder.encode("plainpass")
            userMapper.toEntity(userDto, hashed)
            userRepository.save(userEntity)
            userMapper.toDto(savedEntity)
        }
    }

    @Test
    fun `registerUser propagates exception when repository save fails`() {
        val rawPassword = "pass"
        val userDto = UserDTO(username = "dupe", email = "dupe@email.com")
        val hashed = "hashed"
        val userEntity = User(username = "dupe", email = "dupe@email.com", passwordHash = hashed)

        every { passwordEncoder.encode("pass") } returns hashed
        every { userMapper.toEntity(userDto, hashed) } returns userEntity
        every { userRepository.save(userEntity) } throws DataIntegrityViolationException("duplicate")

        assertThrows<DataIntegrityViolationException> {
            authService.registerUser(userDto, rawPassword)
        }
    }

    @Test
    fun `authenticate returns token and expiration on success`() {
        val authRequest = AuthDTO("user1", "password123")
        val authentication = mockk<Authentication>()
        val jwt = "jwt-token"
        val expiresAt = 1730000000000L

        every { authenticationManager.authenticate(any()) } returns authentication
        every { authentication.name } returns "user1"
        every { jwtTokenService.generateToken(authentication) } returns jwt
        every { jwtTokenService.extractExpirationTime(jwt) } returns expiresAt

        val response = authService.authenticate(authRequest)

        assertEquals(jwt, response.token)
        assertEquals("user1", response.username)
        assertEquals(expiresAt, response.expiresAt)

        verify {
            authenticationManager.authenticate(
                match { it.principal == "user1" && it.credentials == "password123" }
            )
            jwtTokenService.generateToken(authentication)
            jwtTokenService.extractExpirationTime(jwt)
        }
    }

    @Test
    fun `authenticate throws when authentication fails`() {
        val authRequest = AuthDTO("user1", "wrongpass")
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Invalid")

        assertThrows<BadCredentialsException> {
            authService.authenticate(authRequest)
        }

        verify { authenticationManager.authenticate(any()) }
        confirmVerified(jwtTokenService)
    }
}