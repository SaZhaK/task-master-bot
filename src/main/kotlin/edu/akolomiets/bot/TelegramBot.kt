package edu.akolomiets.bot

import edu.akolomiets.bot.config.BotConfig
import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.GoogleCalendarEvent
import edu.akolomiets.bot.entity.enums.BotUserState
import edu.akolomiets.bot.processor.CommandProcessor
import edu.akolomiets.bot.repository.BotUserRepository
import edu.akolomiets.bot.repository.GoogleCalendarEventRepository
import edu.akolomiets.bot.service.GoogleCalendarService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDateTime

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class TelegramBot constructor(
    private val botConfig: BotConfig,
    private val botUserRepository: BotUserRepository,
    private val googleCalendarEventRepository: GoogleCalendarEventRepository
) : TelegramLongPollingBot() {

    @Autowired
    private lateinit var commandProcessors: List<CommandProcessor>

    override fun getBotUsername() = botConfig.botName

    override fun getBotToken() = botConfig.botToken

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId
            val firstName = update.message.chat.firstName

            val command = update.message.text
            if (command == "/start") {
                startCommand(chatId, firstName)
            } else if (command == "/mail") {
                mailCommand(chatId)
            } else if (command == "/createEvent") {
                createEventCommand(chatId)
            } else if (command.startsWith("/")) {
                unknownCommand(chatId, command)
            } else {
                processCommandByState(chatId, command)
            }
        }
    }

    private fun processCommandByState(chatId: Long, command: String) {
        val botUser = botUserRepository.findByChatId(chatId)!! // TODO
        val botUserState = botUser.state ?: error("State not specified")

        commandProcessors
            .asSequence()
            .filter { it.accept(botUserState, command) }
            .map { it.process(botUser, chatId, command) }
            .forEach { sendMessage(chatId, it) }
    }

    private fun createEventCommand(chatId: Long) {
        val botUser = botUserRepository.findByChatId(chatId)!!

        val newEvent = GoogleCalendarEvent().apply {
            this.botUserId = botUser.id
        }
        googleCalendarEventRepository.save(newEvent)

        botUser.state = BotUserState.SUMMARY_CREATION

        botUserRepository.save(botUser)

        val answer = """
            Расскажите кратко, что за событие должно произойти?
            """.trimIndent()

        sendMessage(chatId, answer)
    }

    private fun startCommand(chatId: Long, name: String) {
        val answer = """
            Здравствуйте, $name! Я TaskMaster - Ваш персональный бот-помощник.
            Я понимаю следующие команды:
            - /start - начать диалог
            - /mail - указать почту
            - /createEvent - создать событие
            """.trimIndent()

        sendMessage(chatId, answer)

        var botUser = botUserRepository.findByChatId(chatId)
        if (botUser == null) {
            val newBotUser = BotUser().apply {
                this.chatId = chatId
                this.lastMessageTime = LocalDateTime.now()
                this.state = BotUserState.IDLE
            }
            botUserRepository.save(newBotUser)
            botUser = newBotUser
        } else {
            botUser.apply {
                this.lastMessageTime = LocalDateTime.now()
            }
            botUserRepository.save(botUser)
        }

        if (botUser.gmail == null) {
            val answer2 = """
            Укажите, пожалуйста, Ваш адрес электронной почты, чтобы я мог создавать для Вас события в календаре.
            """.trimIndent()

            sendMessage(chatId, answer2)

            botUser.state = BotUserState.LOGIN
            botUserRepository.save(botUser)
        }
    }

    private fun mailCommand(chatId: Long) {
        val botUser = botUserRepository.findByChatId(chatId)!! // TODO if null

        val answer = """
            Введите, пожалуйста, Ваш адрес электронной почты, чтобы я мог создавать для Вас события в календаре.
            """.trimIndent()

        sendMessage(chatId, answer)

        botUser.state = BotUserState.LOGIN
        botUserRepository.save(botUser)
    }

    private fun unknownCommand(chatId: Long, command: String) {
        val answer = """
            К сожалению, я не знаю, что значит команда '$command'.
            Я понимаю следующие команды:
            - /start - начать диалог
            - /mail - указать почту
            - /createEvent - создать событие
            """.trimIndent()

        sendMessage(chatId, answer)
    }

    private fun sendMessage(chatId: Long, message: String) {
        val sendMessage = SendMessage()
        sendMessage.chatId = chatId.toString()
        sendMessage.text = message

        execute(sendMessage)
    }
}