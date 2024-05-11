package edu.akolomiets.bot

import edu.akolomiets.bot.config.BotConfig
import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.GoogleCalendarEvent
import edu.akolomiets.bot.entity.enums.BotUserState
import edu.akolomiets.bot.repository.BotUserRepository
import edu.akolomiets.bot.repository.GoogleCalendarEventRepository
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class TelegramBot constructor(
    private val botConfig: BotConfig,
    private val botUserRepository: BotUserRepository,
    private val googleCalendarService: GoogleCalendarService,
    private val googleCalendarEventRepository: GoogleCalendarEventRepository
) : TelegramLongPollingBot() {

    override fun getBotUsername() = botConfig.botName

    override fun getBotToken() = botConfig.botToken

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage() && update.message.hasText()) {
            val chatId = update.message.chatId
            val firstName = update.message.chat.firstName

            val command = update.message.text
            if (command == "/start") {
                startCommand(chatId, firstName)
            } else if (command.startsWith("/mail")) {
                mailCommand(chatId)
            } else if (command.startsWith("/createEvent")){
                createEventCommand(chatId)
            } else if (command.startsWith("/")) {
                unknownCommand(chatId, command)
            } else {
                processCommandByState(chatId, command)
            }
        }
    }

    private fun processCommandByState(chatId: Long, command: String) {
        val botUser = botUserRepository.findByChatId(chatId)!!

        if (botUser.state == BotUserState.SUMMARY_CREATION) {
            val answer = """
            Когда начнется это событие?
            Напишите дату в таком формате: 30.04.2024,19:00
            """.trimIndent()

            sendMessage(chatId, answer)

            val event = googleCalendarEventRepository.findByBotUserId(botUser.id!!)!!
            event.apply {
                this.summary = command
            }
            googleCalendarEventRepository.save(event)

            botUser.apply {
                this.state = BotUserState.START_TIME_CREATION
            }
            botUserRepository.save(botUser)
        } else if (botUser.state == BotUserState.START_TIME_CREATION) {
            val answer = """
            Когда завершится это событие?
            Напишите дату в таком формате: 30.04.2024,20:00
            """.trimIndent()

            sendMessage(chatId, answer)

            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy,HH:mm")
            val startDate = LocalDateTime.from(dateTimeFormatter.parse(command))
            val event = googleCalendarEventRepository.findByBotUserId(botUser.id!!)!!
            event.apply {
                this.startDate = startDate
            }
            googleCalendarEventRepository.save(event)

            botUser.apply {
                this.state = BotUserState.END_TIME_CREATION
            }
            botUserRepository.save(botUser)
        } else if (botUser.state == BotUserState.END_TIME_CREATION) {
            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy,HH:mm")
            val endDate = LocalDateTime.from(dateTimeFormatter.parse(command))
            val event = googleCalendarEventRepository.findByBotUserId(botUser.id!!)!!
            event.apply {
                this.endDate = endDate
            }

            val gmail = botUser.gmail
            val link = googleCalendarService.createEvent(gmail, event)

            val answer = """
            Создал событие в календаре и пригласил Вас.
            Событие доступно по ссылке - ${link}.
            Вы получите напоминание о событии за день до его начала.
            """.trimIndent()

            sendMessage(chatId, answer)

            googleCalendarEventRepository.delete(event)

            botUser.apply {
                this.state = BotUserState.IDLE
            }
            botUserRepository.save(botUser)
        } else if (botUser.state == BotUserState.LOGIN) {
            botUser.apply {
                this.gmail = command
                this.state = BotUserState.IDLE
            }
            botUserRepository.save(botUser)

            val answer = """
            Запомнил Вашу почту, теперь смогу создавать для Вас заметки в календаре.
            """.trimIndent()

            sendMessage(chatId, answer)
        }
    }

    private fun createEventCommand(chatId: Long) {
        val answer = """
            Расскажите кратко, что должно произойти.
            """.trimIndent()

        sendMessage(chatId, answer)

        val botUser = botUserRepository.findByChatId(chatId)!!

        val newEvent = GoogleCalendarEvent().apply {
            this.botUserId = botUser.id
        }
        googleCalendarEventRepository.save(newEvent)

        botUser.state = BotUserState.SUMMARY_CREATION

        botUserRepository.save(botUser)
    }

    private fun startCommand(chatId: Long, name: String) {
        val answer = """
            Здравствуйте, $name! Я TaskMaster - Ващ персональный бот-помощник.
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