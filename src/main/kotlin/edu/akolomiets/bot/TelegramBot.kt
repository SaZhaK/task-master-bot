package edu.akolomiets.bot

import edu.akolomiets.bot.config.BotConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class TelegramBot constructor(
    private val botConfig: BotConfig
) : TelegramLongPollingBot() {

    override fun getBotUsername() = botConfig.botName

    override fun getBotToken() = botConfig.botToken

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId
            commandReceived(chatId, update.message.chat.firstName)
        }
    }

    private fun commandReceived(chatId: Long, name: String) {
        val answer = """
            Hi, $name, nice to meet you!
            """.trimIndent()

        val sendMessage = SendMessage()
        sendMessage.chatId = chatId.toString()
        sendMessage.text = answer

        execute(sendMessage)
    }
}