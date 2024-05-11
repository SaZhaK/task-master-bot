package edu.akolomiets.bot.entity.enums

/**
 * @author akolomiets
 * @since 1.0.0
 */
enum class BotUserState(private val dbKey: String) {
    IDLE("I"),
    SUMMARY_CREATION("SC"),
    START_TIME_CREATION("STC"),
    END_TIME_CREATION("ETC");

    fun getDbKey(): String {
        return dbKey
    }
}