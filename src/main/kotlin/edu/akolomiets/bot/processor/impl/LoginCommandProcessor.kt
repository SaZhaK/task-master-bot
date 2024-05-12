package edu.akolomiets.bot.processor.impl

import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.enums.BotUserState
import edu.akolomiets.bot.processor.CommandProcessor
import edu.akolomiets.bot.repository.BotUserRepository
import org.springframework.stereotype.Component

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Component
class LoginCommandProcessor(
    private val botUserRepository: BotUserRepository
): CommandProcessor {

    override fun accept(botUserState: BotUserState, command: String): Boolean {
        return botUserState == BotUserState.LOGIN
    }

    override fun process(botUser: BotUser, chatId: Long, command: String): String {
        botUser.apply {
            this.gmail = command
            this.state = BotUserState.IDLE
        }
        botUserRepository.save(botUser)

        return """
            Запомнил Вашу почту, теперь смогу создавать для Вас заметки в календаре.
            """.trimIndent()
    }
}