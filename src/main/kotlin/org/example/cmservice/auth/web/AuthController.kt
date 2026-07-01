package org.example.cmservice.auth.web

import jakarta.validation.Valid
import org.example.cmservice.auth.mapper.AuthMapper
import org.example.cmservice.auth.mapper.UserMapper
import org.example.cmservice.auth.service.AuthService
import org.example.cmservice.auth.web.dto.AuthRequest
import org.example.cmservice.auth.web.dto.AuthResponse
import org.example.cmservice.auth.web.dto.UserRequest
import org.example.cmservice.auth.web.dto.UserResponse
import org.springframework.web.bind.annotation.*


@RestController()
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val userMapper: UserMapper,
    private val authMapper: AuthMapper
) {

    @GetMapping
    fun helloWorld(): String = "Hello world, this works"

    @PostMapping("/register")
    fun registerUser(@Valid @RequestBody user: UserRequest): UserResponse {
        val dto = userMapper.toDto(user)
        val result = authService.registerUser(dto, user.rawPassword)
        return userMapper.toResponse(result)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody authRequest: AuthRequest): AuthResponse =
        authMapper.fromDto(authService.authenticate(authMapper.toDto(authRequest)))
}