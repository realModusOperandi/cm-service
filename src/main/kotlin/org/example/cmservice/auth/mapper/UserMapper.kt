package org.example.cmservice.auth.mapper

import org.example.cmservice.auth.domain.User
import org.example.cmservice.auth.service.dto.UserDTO
import org.example.cmservice.auth.web.dto.UserRequest
import org.example.cmservice.auth.web.dto.UserResponse
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    fun toDto(request: UserRequest): UserDTO

    fun toDto(entity: User): UserDTO

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "passwordHash")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.OffsetDateTime.now())")
    fun toEntity(dto: UserDTO, passwordHash: String): User

    fun toResponse(dto: UserDTO): UserResponse
}