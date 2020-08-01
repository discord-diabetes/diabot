package com.dongtronic.diabot.platforms.discord.commands.admin.rewards

import com.dongtronic.diabot.data.redis.RewardDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.RoleUtils
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Role
import java.util.*

class AdminRewardListCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger by Logger()

    init {
        this.name = "list"
        this.help = "List configured rewards"
        this.guildOnly = true
        this.aliases = arrayOf("l", "ls")
    }

    override fun execute(event: CommandEvent) {
        val configuredRewards = RewardDAO.getInstance().getSimpleRewards(event.guild.id)

        val rewards = RoleUtils.buildRewardsMap(configuredRewards, event.guild)
        val builder = EmbedBuilder()

        buildRewardResponse(builder, rewards, event)

        event.reply(builder.build())
    }

    private fun buildRewardResponse(builder: EmbedBuilder, rewards: TreeMap<Role, MutableList<Role>>, event: CommandEvent) {
        builder.setTitle("${rewards.size} Rewards for ${event.guild.name}")
        builder.setColor(java.awt.Color.ORANGE)

        for ((required, rewardList) in rewards) {
            val rewardString = StringBuilder()
            for (reward in rewardList) {
                rewardString.append(reward.name)
                rewardString.append(", ")
            }
            rewardString.trimEnd(',', ' ')

            builder.appendDescription("**")
            builder.appendDescription(required.name)
            builder.appendDescription("** => ")
            builder.appendDescription(rewardString.toString())
            builder.appendDescription("\n")
        }
    }
}
