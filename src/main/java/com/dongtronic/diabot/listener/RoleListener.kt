package com.dongtronic.diabot.listener

import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.util.RoleUtils
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class RoleListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(RoleListener::class.java)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return

        val author = event.author
        val guild = event.guild
        val member = guild.getMember(author) ?: return

        val userRoles = member.roles

        if(RewardDAO.getInstance().getOptOut(guild.id, author.id)) {
            // Skip users that have opted out
            return
        }

        val potentialRewards = RewardDAO.getInstance().getSimpleRewards(guild.id)

        val rewards = RoleUtils.buildRewardsMap(potentialRewards, guild)

        // Check if user applies for new rewards
        for ((required, rewardList) in rewards) {
            if (userRoles.contains(required)) {
                for(reward in rewardList) {
                    if(!userRoles.contains(reward)) {
                        guild.addRoleToMember(member, reward).queue()
                        logger.info("Assigning reward roles ${reward.name} (${reward.id}) to ${author.name}")
                    }
                }
            }
        }
    }


}
