package com.dongtronic.diabot.data

import com.fasterxml.jackson.annotation.JsonAutoDetect

/**
 * @property quoteId the quote's ID
 * @property guildId the guild in which this quote belongs to
 * @property author the quote's author
 * @property authorId the author's user ID
 * @property message the quote message
 * @property messageId the quote's message ID
 * @property time quote creation in unix time
 */
@JsonAutoDetect
data class QuoteDTO(
        val quoteId: Long? = null,
        val guildId: Long,
        val author: String,
        val authorId: Long = 0,
        val message: String,
        val messageId: Long,
        val time: Long = System.currentTimeMillis() / 1000
)

/**
 * @property guildId the guild ID
 * @property quoteIndex this guild's current quote index
 */
@JsonAutoDetect
data class QuoteIndexDTO(
        val guildId: Long,
        val quoteIndex: Long = 1
)