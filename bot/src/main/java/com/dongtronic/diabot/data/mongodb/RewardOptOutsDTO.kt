package com.dongtronic.diabot.data.mongodb

/**
 * The list of users who are opted out of rewards in a guild
 */
data class RewardOptOutsDTO(
    val guildId: String,
    val optOut: List<String> = emptyList()
)
