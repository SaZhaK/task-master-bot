package edu.akolomiets.bot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication
@EnableJpaRepositories
class BotApplication

fun main(args: Array<String>) {
	runApplication<BotApplication>(*args)
}
