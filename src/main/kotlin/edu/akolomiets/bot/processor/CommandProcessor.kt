package edu.akolomiets.bot.processor

import edu.akolomiets.bot.entity.BotUser
import edu.akolomiets.bot.entity.enums.BotUserState

/**
 * @author akolomiets
 * @since 1.0.0
 */
interface CommandProcessor {

    fun accept(botUserState: BotUserState, command: String): Boolean

    fun process(botUser: BotUser, chatId: Long, command: String): String
}