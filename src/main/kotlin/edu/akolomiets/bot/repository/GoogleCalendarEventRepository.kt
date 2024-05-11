package edu.akolomiets.bot.repository

import edu.akolomiets.bot.entity.GoogleCalendarEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Repository
interface GoogleCalendarEventRepository: JpaRepository<GoogleCalendarEvent, Long> {

    fun findByBotUserId(botUserId: Long): GoogleCalendarEvent?
}