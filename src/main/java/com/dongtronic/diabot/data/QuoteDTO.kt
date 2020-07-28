package com.dongtronic.diabot.data


/**
 * @property id the quote's ID
 * @property author the quote's author
 * @property authorId the author's user ID
 * @property message the quote message
 * @property messageId the quote's message ID
 * @property time quote creation in unix time
 */
data class QuoteDTO(
        val id: String = "",
        val author: String,
        val authorId: Long = 0,
        val message: String,
        val messageId: Long,
        val time: Long = System.currentTimeMillis() / 1000
)