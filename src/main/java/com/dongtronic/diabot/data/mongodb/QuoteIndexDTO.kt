package com.dongtronic.diabot.data.mongodb

import com.fasterxml.jackson.annotation.JsonAutoDetect

/**
 * @property guildId the guild ID
 * @property quoteIndex this guild's current quote index
 */
@JsonAutoDetect
data class QuoteIndexDTO(
        val guildId: String,
        val quoteIndex: Long = 1
)