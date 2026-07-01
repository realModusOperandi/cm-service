package org.example.cmservice.auth.repository

import org.example.cmservice.IntegrationTestBase
import org.example.cmservice.auth.domain.User
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import kotlin.test.Test
import kotlin.test.assertEquals

@Transactional
class UserRepositoryIntegrationTest @Autowired constructor(val userRepository: UserRepository) : IntegrationTestBase() {

    @Test
    fun `save and find user`() {
        val user = User(username = "test user", email = "test@email.com", passwordHash = "hashed")
        userRepository.save(user)

        val found = userRepository.findByUsername("test user")

        assertNotNull(found)
        assertEquals("test@email.com", found.email)
    }

    @Test
    fun `find all users saved`() {
        val user1 = User(username = "test user 1", email = "test1@email.com", passwordHash = "hashed")
        val user2 = User(username = "test user 2", email = "test2@email.com", passwordHash = "hashed")

        userRepository.save(user1)
        userRepository.save(user2)

        val result = userRepository.findAll()

        assertEquals(2, result.size)
        assertEquals(user1, result[0])
        assertEquals(user2, result[1])
    }

    @Test
    fun `find specific user by id`() {
        val user1 = userRepository.save(User(username = "test user 1", email = "test1@email.com", passwordHash = "hashed"))

        val result = userRepository.findByIdOrNull(user1.id)

        assertNotNull(result)
        assertEquals(user1.id, result.id)
    }

    @Test
    fun `find specific user by id not found`() {
        val result = userRepository.findByIdOrNull(2843904820)

        assertNull(result)
    }
}