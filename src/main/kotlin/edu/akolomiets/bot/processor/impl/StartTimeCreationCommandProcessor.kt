package edu.akolomiets.bot.processor.impl

import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.enums.BotUserState
import edu.akolomiets.bot.processor.CommandProcessor
import edu.akolomiets.bot.repository.BotUserRepository
import edu.akolomiets.bot.repository.GoogleCalendarEventRepository
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class StartTimeCreationCommandProcessor(
    private val botUserRepository: BotUserRepository,
    private val googleCalendarEventRepository: GoogleCalendarEventRepository
) : CommandProcessor {

    override fun accept(botUserState: BotUserState, command: String): Boolean {
        return botUserState == BotUserState.START_TIME_CREATION
    }

    override fun process(botUser: BotUser, chatId: Long, command: String): String {
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

        return """
            Когда завершится это событие?
            Напишите дату в таком формате: 30.04.2024,20:00
            """.trimIndent()
    }
}