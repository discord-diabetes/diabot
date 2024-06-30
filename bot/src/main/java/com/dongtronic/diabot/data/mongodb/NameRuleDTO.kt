package com.dongtronic.diabot.data.mongodb

data class NameRuleDTO(
    val guildId: String,
    val enforce: Boolean = false,
    val pattern: String = "",
    val hintMessage: String = ""
)
