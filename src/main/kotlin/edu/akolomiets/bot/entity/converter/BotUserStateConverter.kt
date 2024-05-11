package edu.akolomiets.bot.entity.converter

import edu.akolomiets.bot.entity.enums.BotUserState
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

/**
 * @author akolomiets
 * @since 1.0.0
 */
@Converter(autoApply = true)
class BotUserStateConverter : AttributeConverter<BotUserState, String> {

    override fun convertToDatabaseColumn(botUserState: BotUserState): String {
        return botUserState.getDbKey()
    }

    override fun convertToEntityAttribute(code: String): BotUserState? {
        return BotUserState.values().firstOrNull { it.getDbKey() == code }
    }
}