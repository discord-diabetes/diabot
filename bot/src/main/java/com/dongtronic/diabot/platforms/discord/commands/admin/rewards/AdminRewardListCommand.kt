package com.dongtronic.diabot.platforms.discord.commands.admin.rewards

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.RoleUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Role
import java.util.*

class AdminRewardListCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "list"
        this.help = "List configured rewards"
        this.guildOnly = true
        this.aliases = arrayOf("l", "ls")
    }

    override fun execute(event: CommandEvent) {
        RewardsDAO.instance.getRewards(event.guild.id)
            .defaultIfEmpty(emptyList())
            .subscribe({ dtos ->
                val rewards = RoleUtils.buildRewardsMap(dtos, event.guild)
                val builder = EmbedBuilder()
                buildRewardResponse(builder, rewards, event)
                event.reply(builder.build())
            }, {
                logger.warn("Could not retrieve rewards for guild ${event.guild.id}", it)
                event.replyError("Could not retrieve list of rewards for this guild")
            })
    }

    private fun buildRewardResponse(builder: EmbedBuilder, rewards: TreeMap<Role, List<Role>>, event: CommandEvent) {
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
