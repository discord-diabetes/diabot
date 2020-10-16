package com.dongtronic.diabot.platforms.discord.utils

import com.dongtronic.diabot.data.mongodb.RewardsDTO
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Role
import java.util.*

object RoleUtils {
    private val logger = logger()

    fun buildRewardsMap(potentialRewards: List<RewardsDTO>, guild: Guild): TreeMap<Role, List<Role>> {
        val rewards: TreeMap<Role, List<Role>> = TreeMap()

        potentialRewards.forEach { dto ->
            val required = guild.getRoleById(dto.requiredRole) ?: return@forEach
            val rewardRoles = dto.roleRewards.mapNotNull { guild.getRoleById(it) }
            rewards[required] = rewardRoles
        }

        return rewards
    }
}
