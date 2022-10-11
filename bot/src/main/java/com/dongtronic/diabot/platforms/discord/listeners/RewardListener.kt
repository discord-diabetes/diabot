package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.data.mongodb.RewardsDTO
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import net.dv8tion.jda.api.hooks.ListenerAdapter

class RewardListener : ListenerAdapter() {
    private val logger = logger()

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (!event.isFromGuild) return
        if (!event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) return
        if (event.author.isBot) return

        val author = event.author
        val guild = event.guild
        val member = guild.getMember(author) ?: return

        RewardsDAO.instance.isOptOut(guild.id, author.id)
                // if the user isn't opted out
                .filter { !it }
                // get the rewards for this guild
                .flatMap { RewardsDAO.instance.getRewards(guild.id) }
                .doOnNext { rewards ->
                    rewards.forEach { applyRoles(member, it) }
                }
                .subscribe({}, {
                    if (it is InsufficientPermissionException) {
                        logger.warn("Could not assign reward to user ${member.effectiveName} due to lack of permission")
                    } else {
                        logger.warn("Could not assign reward to user ${member.effectiveName}", it)
                    }
                })
    }

    private fun applyRoles(member: Member, dto: RewardsDTO) {
        val userRoles = member.roles
        val required = dto.requiredToRole(member.guild) ?: return
        if (!userRoles.contains(required)) return

        val rewards = dto.rewardsToRoles(member.guild)

        rewards.forEach {
            if (userRoles.contains(it)) return@forEach

            member.guild.addRoleToMember(member, it).queue()
            logger.info("Assigning reward roles ${it.name} (${it.id}) to ${member.effectiveName}")
        }
    }
}
