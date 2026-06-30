package no.novari.flyt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["no.novari"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
