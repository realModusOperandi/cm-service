package org.example.cmservice.auth.web.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class UserRequest(
    @field:NotBlank(message = "Username is required")
    val username: String = "",

    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email cannot be blank")
    val email: String = "",

    @field:NotBlank(message = "Password is required")
    val rawPassword: String = "",
)
