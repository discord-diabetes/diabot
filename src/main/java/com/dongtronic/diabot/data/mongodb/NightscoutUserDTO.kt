package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutSetDisplayCommand
import com.fasterxml.jackson.annotation.JsonAutoDetect

@JsonAutoDetect
data class NightscoutUserDTO(
        val userId: Long,
        val url: String,
        val token: String? = null,
        val displayOptions: List<String> = NightscoutSetDisplayCommand.enabledOptions.toList(),
        val publicGuilds: List<Long> = emptyList()
) {
    var avatarUrl: String? = null
        set(value) {
            // only set once
            field = field ?: value
        }
}