package com.dongtronic.diabot.data.mongodb

import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutSetDisplayCommand
import com.fasterxml.jackson.annotation.JsonAutoDetect

@JsonAutoDetect
data class NightscoutUserDTO(
        val userId: Long,
        val url: String? = null,
        val token: String? = null,
        val displayOptions: List<String> = NightscoutSetDisplayCommand.enabledOptions.toList(),
        val publicGuilds: List<Long> = emptyList()
) {
    var avatarUrl: String? = null
        set(value) {
            // only set once
            field = field ?: value
        }

    /**
     * Checks if this NS user has set their NS to be public in the provided guild.
     *
     * @param guildId The ID of the guild
     * @return Whether this Nightscout is public in this guild.
     */
    fun isNightscoutPublic(guildId: Long): Boolean {
        return publicGuilds.contains(guildId)
    }
}