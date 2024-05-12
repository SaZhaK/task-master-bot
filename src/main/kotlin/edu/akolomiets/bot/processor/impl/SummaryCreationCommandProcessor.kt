package edu.akolomiets.bot.processor.impl

import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.enums.BotUserState
import edu.akolomiets.bot.processor.CommandProcessor
import edu.akolomiets.bot.repository.BotUserRepository
import edu.akolomiets.bot.repository.GoogleCalendarEventRepository
import org.springframework.stereotype.Component

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class SummaryCreationCommandProcessor(
    private val botUserRepository: BotUserRepository,
    private val googleCalendarEventRepository: GoogleCalendarEventRepository
) : CommandProcessor {

    override fun accept(botUserState: BotUserState, command: String): Boolean {
        return botUserState == BotUserState.SUMMARY_CREATION
    }

    override fun process(botUser: BotUser, chatId: Long, command: String): String {
        val event = googleCalendarEventRepository.findByBotUserId(botUser.id!!)!!
        event.apply {
            this.summary = command
        }
        googleCalendarEventRepository.save(event)

        botUser.apply {
            this.state = BotUserState.START_TIME_CREATION
        }
        botUserRepository.save(botUser)

        return """
            Когда начнется это событие?
            Напишите дату в таком формате: 30.04.2024,19:00
            """.trimIndent()
    }
}