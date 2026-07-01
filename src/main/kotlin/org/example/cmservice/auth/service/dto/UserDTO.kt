package org.example.cmservice.auth.service.dto

import java.time.OffsetDateTime

data class UserDTO(
    var id: Long? = null,
    var username: String,
    var email: String,
    var createdAt: OffsetDateTime? = null,
    var updatedAt: OffsetDateTime? = null
)
