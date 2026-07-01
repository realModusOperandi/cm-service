package org.example.cmservice.auth.mapper

import org.example.cmservice.auth.service.dto.AuthDTO
import org.example.cmservice.auth.service.dto.TokenDTO
import org.example.cmservice.auth.web.dto.AuthRequest
import org.example.cmservice.auth.web.dto.AuthResponse
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface AuthMapper {
    fun toDto(request: AuthRequest): AuthDTO

    fun fromDto(dto: TokenDTO): AuthResponse
}