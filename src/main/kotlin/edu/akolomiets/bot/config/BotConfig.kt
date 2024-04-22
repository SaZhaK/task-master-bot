package edu.akolomiets.bot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Configuration
@PropertySource("application.properties")
class BotConfig {

    @Value("\${bot.name}")
    lateinit var botName: String

    @Value("\${bot.token}")
    lateinit var botToken: String
}