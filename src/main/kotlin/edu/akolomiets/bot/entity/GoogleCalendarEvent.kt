package edu.akolomiets.bot.entity

import edu.akolomiets.bot.entity.GoogleCalendarEvent.Companion.TABLE_NAME
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Entity(name = TABLE_NAME)
@Table(name = TABLE_NAME)
class GoogleCalendarEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null

    @Column(name = "bot_user_id")
    var botUserId: Long? = null

    @Column(name = "summary")
    var summary: String? = null

    @Column(name = "start_date")
    var startDate: LocalDateTime? = null

    @Column(name = "end_date")
    var endDate: LocalDateTime? = null

    companion object {
        const val TABLE_NAME = "google_calendar_event"
    }
}