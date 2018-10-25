package com.dongtronic.diabot.listener

import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.util.RoleUtils
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.managers.GuildController
import org.slf4j.LoggerFactory

class RoleListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(RoleListener::class.java)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent?) {
        if (event!!.author.isBot) return

        val author = event.author
        val guild = event.guild
        val member = guild.getMember(author)
        val userRoles = member.roles
        val guildManager = GuildController(event.guild)

        val potentialRewards = RewardDAO.getInstance().getSimpleRewards(guild.id)

        val rewards = RoleUtils.buildRewardsMap(potentialRewards, guild)

        // Check if user applies for new rewards
        for ((required, rewardList) in rewards) {
            if (userRoles.contains(required)) {
                // Give member all reward roles for given requirement
                guildManager.addRolesToMember(member, rewardList).queue()

                logger.info("Assigning reward roles $rewardList to ${author.name}")
            }
        }
    }


}
