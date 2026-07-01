package org.example.cmservice.auth.service.dto

data class TokenDTO(
    val token: String,
    val username: String,
    val expiresAt: Long,
)
