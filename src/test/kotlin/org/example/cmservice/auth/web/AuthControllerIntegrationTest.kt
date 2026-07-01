package org.example.cmservice.auth.web

import com.jayway.jsonpath.JsonPath
import org.example.cmservice.IntegrationTestBase
import org.example.cmservice.auth.domain.User
import org.example.cmservice.auth.repository.UserRepository
import org.example.cmservice.auth.service.JwtTokenService
import org.example.cmservice.auth.web.dto.AuthRequest
import org.example.cmservice.auth.web.dto.UserRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthControllerIntegrationTest @Autowired constructor(
    val userRepository: UserRepository,
    val mvc: MockMvc,
    val objectMapper: ObjectMapper,
    val passwordEncoder: PasswordEncoder,
    val jwtTokenService: JwtTokenService,
) : IntegrationTestBase() {
    companion object {
        const val USERNAME_REQUIRED = "Username is required"
        const val EMAIL_REQUIRED = "Email cannot be blank"
        const val EMAIL_INVALID = "Invalid email format"
        const val PASSWORD_REQUIRED = "Password is required"
    }

    @AfterEach
    fun tearDown() {
        userRepository.deleteAll()
    }

    @Test
    fun `POST user registration successful`() {
        val user = UserRequest(username = "test user", email = "test@email.com", rawPassword = "pass")

        mvc.perform(
            post("/api/auth/register")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(user.username))

        val dbUser = userRepository.findByUsername(user.username)
        assertNotNull(dbUser)
        assertEquals(user.email, dbUser.email)
    }

    @Test
    fun `POST user registration successful for second user`() {
        val user1 = UserRequest(username = "test user", email = "test@email.com", rawPassword = "pass")
        val user2 = UserRequest(username = "second user", email = "partdeux@email.com", rawPassword = "pass")

        mvc.perform(
            post("/api/auth/register")
                .content(objectMapper.writeValueAsString(user1))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(user1.username))

        val dbUser1 = userRepository.findByUsername(user1.username)
        assertNotNull(dbUser1)
        assertEquals(user1.email, dbUser1.email)

        mvc.perform(
            post("/api/auth/register")
                .content(objectMapper.writeValueAsString(user2))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(user2.username))

        val dbUser2 = userRepository.findByUsername(user2.username)
        assertNotNull(dbUser2)
        assertEquals(user2.email, dbUser2.email)
    }

    @Test
    fun `POST user registration blank username`() {
        val user = UserRequest(username = "", email = "test@email.com", rawPassword = "pass")

        mvc.perform(
            post("/api/auth/register")
                .content(objectMapper.writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `POST user registration null username`() {
        val user = """
            {
                "email": "test@email.com",
                "rawPassword": "pass"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/auth/register")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.username").value(USERNAME_REQUIRED))

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `POST user registration null email`() {
        val user = """
            {
                "username": "test",
                "rawPassword": "pass"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/auth/register")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.email").value(EMAIL_REQUIRED))

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `POST user registration invalid email`() {
        val user = """
            {
                "username": "test",
                "email": "testBAD",
                "rawPassword": "pass"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/auth/register")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.email").value(EMAIL_INVALID))

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `POST user registration null password`() {
        val user = """
            {
                "username": "test",
                "email": "test@email.com"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/auth/register")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.rawPassword").value(PASSWORD_REQUIRED))

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `POST user login successful`() {
        val rawPassword = "password"
        val user = userRepository.save(
            User(
                username = "test 1",
                email = "test1@email.com",
                passwordHash = passwordEncoder.encode(rawPassword)!!
            )
        )
        val authRequest = AuthRequest(user.username, rawPassword)

        println(user.id)

        val result = mvc.perform(
            post("/api/auth/login")
                .content(objectMapper.writeValueAsString(authRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.username").value(user.username))
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.expiresAt").isNumber)
            .andReturn()

        val response = result.response.contentAsString
        val token = JsonPath.read<String>(response, "$.token")
        val decoded = jwtTokenService.decoder.decode(token)
        assertEquals(user.username, decoded.subject)

        val expiresAtMillis = JsonPath.read<Long>(response, "$.expiresAt")
        val now = Instant.now().toEpochMilli()
        assertTrue(expiresAtMillis > now, "expiresAt should be in the future")
        val diffSeconds = (expiresAtMillis - now) / 1000
        assertTrue(
            diffSeconds in 3590..3610,
            "expiresAt should be about 3600 seconds in the future (was $diffSeconds)"
        )
    }

    @Test
    fun `POST user login fails wrong password`() {
        val rawPassword = "password"
        val user = userRepository.save(User(username = "test 1", email = "test1@email.com", passwordHash = passwordEncoder.encode(rawPassword)!!))
        val authRequest = AuthRequest(user.username, "wrong")

        mvc.perform(
            post("/api/auth/login")
                .content(objectMapper.writeValueAsString(authRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST user login fails wrong username`() {
        val rawPassword = "password"
        val user = userRepository.save(User(username = "test 1", email = "test1@email.com", passwordHash = passwordEncoder.encode(rawPassword)!!))
        val authRequest = AuthRequest("wrong", "wrong")

        mvc.perform(
            post("/api/auth/login")
                .content(objectMapper.writeValueAsString(authRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST user login null username`() {
        val user = """
            {
                "password": "pass"
            }
        """.trimIndent()

        mvc.perform(
            post("/api/auth/login")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.username").value(org.example.cmservice.auth.web.AuthControllerIntegrationTest.Companion.USERNAME_REQUIRED))

        assertEquals(0, userRepository.findAll().count())
    }

    @Test
    fun `POST user login null password`() {
        val user = """
            {
                "username": "test"
            }
        """.trimIndent()

        val result = mvc.perform(
            post("/api/auth/login")
                .content(user)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.password").value(org.example.cmservice.auth.web.AuthControllerIntegrationTest.Companion.PASSWORD_REQUIRED))


        assertEquals(0, userRepository.findAll().count())
    }
}