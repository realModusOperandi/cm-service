package org.example.cmservice.auth.web.dto

data class AuthResponse(
    var token: String,
    var username: String,
    var expiresAt: Long,
)
