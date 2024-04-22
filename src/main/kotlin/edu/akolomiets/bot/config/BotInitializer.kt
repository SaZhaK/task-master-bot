package edu.akolomiets.bot.config

import edu.akolomiets.bot.TelegramBot
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class BotInitializer constructor(
    private val telegramBot: TelegramBot
) {

    @EventListener(ContextRefreshedEvent::class)
    @Throws(TelegramApiException::class)
    fun init() {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(telegramBot)
    }

}