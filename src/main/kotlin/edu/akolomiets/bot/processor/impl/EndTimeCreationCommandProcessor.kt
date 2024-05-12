package edu.akolomiets.bot.processor.impl

import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.enums.BotUserState
import edu.akolomiets.bot.processor.CommandProcessor
import edu.akolomiets.bot.repository.BotUserRepository
import edu.akolomiets.bot.repository.GoogleCalendarEventRepository
import edu.akolomiets.bot.service.GoogleCalendarService
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class EndTimeCreationCommandProcessor(
    private val botUserRepository: BotUserRepository,
    private val googleCalendarEventRepository: GoogleCalendarEventRepository,
    private val googleCalendarService: GoogleCalendarService
): CommandProcessor {

    override fun accept(botUserState: BotUserState, command: String): Boolean {
        return botUserState == BotUserState.END_TIME_CREATION
    }

    override fun process(botUser: BotUser, chatId: Long, command: String): String {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy,HH:mm")
        val endDate = LocalDateTime.from(dateTimeFormatter.parse(command))
        val event = googleCalendarEventRepository.findByBotUserId(botUser.id!!)!!
        event.apply {
            this.endDate = endDate
        }

        val gmail = botUser.gmail
        val link = googleCalendarService.createEvent(gmail, event)

        googleCalendarEventRepository.delete(event)

        botUser.apply {
            this.state = BotUserState.IDLE
        }
        botUserRepository.save(botUser)

        return """
            Создал событие в календаре и пригласил Вас.
            Событие доступно по ссылке - ${link}.
            Вы получите напоминание о событии за день до его начала.
            """.trimIndent()
    }
}