package org.example.cmservice.auth.web.dto

data class UserResponse(
    val id: Long,
    val username: String,
    val email: String
)
