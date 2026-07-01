package org.example.cmservice.auth.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.cmservice.auth.domain.User
import org.example.cmservice.auth.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class AppUserDetailsServiceTest {

    @MockK
    lateinit var userRepository: UserRepository
    lateinit var userDetailsService: AppUserDetailsService

    @BeforeEach
    fun setup() {
        userDetailsService = AppUserDetailsService(userRepository)
    }

    @Test
    fun `loadUserByUsername returns UserDetails when user exists`() {
        val user = User(username = "test", email = "test@email.com", passwordHash = "hashed")
        every { userRepository.findByUsername("test") } returns user

        val details = userDetailsService.loadUserByUsername("test")

        assertEquals("test", details.username)
        assertEquals("hashed", details.password)
    }

    @Test
    fun `loadUserByUsername throws when user not found`() {
        every { userRepository.findByUsername("missing") } returns null

        assertThrows<UsernameNotFoundException> {
            userDetailsService.loadUserByUsername("missing")
        }
    }
}