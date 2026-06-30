package org.example.cmservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class CmServiceApplication

fun main(args: Array<String>) {
    runApplication<CmServiceApplication>(*args)
}
