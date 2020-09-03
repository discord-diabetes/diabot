package com.dongtronic.diabot.data.mongodb

import com.fasterxml.jackson.annotation.JsonAutoDetect

/**
 * @property quoteId the quote's ID
 * @property guildId the guild in which this quote belongs to
 * @property channelId the channel which this quote is located under
 * @property author the quote's author
 * @property authorId the author's user ID
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
        val message: String,
        val messageId: String,
        val time: Long = System.currentTimeMillis() / 1000
)