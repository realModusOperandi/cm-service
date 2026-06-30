package org.example.cmservice

import org.example.cmservice.config.TestcontainersConfiguration
import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<CmServiceApplication>().with(TestcontainersConfiguration::class).run(*args)
}
