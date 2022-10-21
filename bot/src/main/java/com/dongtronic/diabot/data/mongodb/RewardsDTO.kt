package com.dongtronic.diabot.data.mongodb

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role

/**
 * Holds the rewards that a (required) role gives
 */
data class RewardsDTO(
        val guildId: String,
        val requiredRole: String,
        val roleRewards: List<String> = emptyList()
) {
    /**
     * Converts the required role ID to a [Role].
     *
     * @param guild The guild this role ID belongs to
     * @return [Role] if found, null otherwise
     */
    fun requiredToRole(guild: Guild): Role? {
        return guild.getRoleById(requiredRole)
    }

    /**
     * Converts all role reward IDs to [Role]s.
     *
     * @param guild The guild these role reward IDs belong to
     * @return A list of [Role]s
     */
    fun rewardsToRoles(guild: Guild): List<Role> {
        return roleRewards.mapNotNull { guild.getRoleById(it) }
    }
}
