package org.example.cmservice.auth.service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.example.cmservice.auth.service.dto.UserPrincipal
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import java.time.Duration
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class JwtTokenServiceTest {

    @MockK lateinit var encoder: JwtEncoder
    @MockK lateinit var decoder: JwtDecoder
    lateinit var jwtTokenService: JwtTokenService

    @BeforeEach
    fun setup() {
        jwtTokenService = JwtTokenService(encoder, decoder)
    }

    @Test
    fun `generateToken should include uid claim from principal id`() {
        // Arrange
        val authentication = mockk<Authentication>()
        val principal = mockk<UserPrincipal>()

        every { principal.id } returns 42L
        every { principal.username } returns "testUser"
        every { authentication.principal } returns principal

        val jwt = mockk<Jwt>()
        every { jwt.tokenValue } returns "mocked-jwt-token"
        every { encoder.encode(any()) } returns jwt

        val result = jwtTokenService.generateToken(authentication)

        assertEquals("mocked-jwt-token", result)

        verify {
            encoder.encode(withArg { params ->

                val claims = params.claims
                assertNotNull(claims)
                assertEquals("https://self", claims.issuer.toString())
                assertEquals("testUser", claims.subject)

                assertEquals(42L, claims.claims["uid"])

                val issuedAt = claims.issuedAt
                val expiresAt = claims.expiresAt

                assertNotNull(issuedAt)
                assertNotNull(expiresAt)

                val diffSeconds = Duration.between(issuedAt, expiresAt).seconds
                assertTrue(diffSeconds in 3590..3610)
            })
        }
    }


    @Test
    fun `extractExpirationTime should return epoch millis from token`() {
        val expInstant = Instant.now().plusSeconds(3600)
        val jwt = mockk<Jwt>()
        every { jwt.getClaim<Instant>("exp") } returns expInstant

        every { decoder.decode("token123") } returns jwt

        val result = jwtTokenService.extractExpirationTime("token123")

        assertEquals(expInstant.toEpochMilli(), result)
        verify { decoder.decode("token123") }
    }

    @Test
    fun `extractExpirationTime should throw if exp claim missing`() {
        val jwt = mockk<Jwt>()
        every { jwt.getClaim<Instant>("exp") } returns null
        every { decoder.decode("badtoken") } returns jwt

        assertThrows<NullPointerException> {
            jwtTokenService.extractExpirationTime("badtoken")
        }
    }
}
