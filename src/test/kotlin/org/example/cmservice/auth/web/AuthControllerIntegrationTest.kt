package org.example.cmservice.auth.web

import org.example.cmservice.IntegrationTestBase
import org.example.cmservice.auth.repository.UserRepository
import org.example.cmservice.auth.web.dto.UserRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthControllerIntegrationTest @Autowired constructor(
    val userRepository: UserRepository,
    val mvc: MockMvc,
    val objectMapper: ObjectMapper
) : IntegrationTestBase() {
    companion object {
        const val USERNAME_REQUIRED = "Username is required"
        const val EMAIL_REQUIRED = "Email cannot be blank"
        const val EMAIL_INVALID = "Invalid email format"
        const val PASSWORD_REQUIRED = "Password is required"
    }

    @AfterEach
    fun tearDown() {
        println("Deleting ${userRepository.findAll()}")
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
}