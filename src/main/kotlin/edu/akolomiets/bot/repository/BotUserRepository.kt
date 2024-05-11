package edu.akolomiets.bot.repository

import edu.akolomiets.bot.entity.BotUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Repository
interface BotUserRepository : JpaRepository<BotUser, Long> {

    fun findByChatId(chatId: Long): BotUser?
}