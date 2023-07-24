package com.dongtronic.diabot.data.mongodb

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @property quoteId the quote's ID
 * @property guildId the guild in which this quote belongs to
 * @property channelId the channel which this quote is located under
 * @property author the quote's author
 * @property authorId the author's user ID
 * @property quoterId the user ID of the person who created the quote
 * @property message the quote message
 * @property messageId the quote's message ID
 * @property time quote creation in unix time
 */
@JsonAutoDetect
data class QuoteDTO(
        val quoteId: String? = null,
        val guildId: String,
        val channelId: String,
        val author: String,
        val authorId: String = "",
        val quoterId: String = "",
        val message: String,
        val messageId: String,
        val time: Long = System.currentTimeMillis() / 1000
) {
    /**
     * Checks whether a quote has a message link associated with it.
     * This is done by checking for the guild ID, channel ID, and message ID properties to have proper values.
     *
     * @return if the quote can have a link pointing to its original message
     */
    fun hasMessageLink(): Boolean {
        return guildId != "0" && channelId != "0" && messageId != "0"
    }

    /**
     * Creates a message link for a quote.
     *
     * @return a message link pointing to the quote, otherwise `null` if a link cannot be created
     */
    @JsonIgnore
    fun getMessageLink(): String? {
        if (!hasMessageLink()) {
            return null
        }

        return DISCORD_MESSAGE_LINK.replace("{guild}", guildId)
                .replace("{channel}", channelId)
                .replace("{message}", messageId)
    }

    companion object {
        const val DISCORD_MESSAGE_LINK = "https://discord.com/channels/{guild}/{channel}/{message}"
    }
}
