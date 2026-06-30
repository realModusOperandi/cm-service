package org.example.cmservice.user.web

import org.springframework.web.bind.annotation.*


@RestController()
@RequestMapping("/api/auth")
class UserController() {

    @GetMapping
    fun helloWorld(): String = "Hello world, this works"

}