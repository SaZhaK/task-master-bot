package edu.akolomiets.bot.entity

import edu.akolomiets.bot.entity.BotUser.Companion.TABLE_NAME
import edu.akolomiets.bot.entity.converter.BotUserStateConverter
import edu.akolomiets.bot.entity.enums.BotUserState
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Entity(name = TABLE_NAME)
@Table(name = TABLE_NAME)
class BotUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long? = null

    @Column(name = "chat_id")
    var chatId: Long? = null

    @Column(name = "gmail")
    var gmail: String? = null

    @Column(name = "last_message_time")
    var lastMessageTime: LocalDateTime? = null

    @Column(name = "state")
    @Convert(converter = BotUserStateConverter::class)
    var state: BotUserState? = null

    companion object {
        const val TABLE_NAME = "bot_user"
    }
}