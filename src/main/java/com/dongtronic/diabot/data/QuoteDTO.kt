package com.dongtronic.diabot.data


data class QuoteDTO(
        val id: String,
        val author: String,
        val authorId: Long? = null,
        val message: String,
        val time: Long = System.currentTimeMillis() / 1000
)