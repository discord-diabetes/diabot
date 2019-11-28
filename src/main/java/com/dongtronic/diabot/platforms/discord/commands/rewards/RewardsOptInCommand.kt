package com.dongtronic.diabot.platforms.discord.commands.rewards

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class RewardsOptInCommand(category: Category, parent: Command) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(RewardsOptInCommand::class.java)

    init {
        this.name = "optin"
        this.help = "Opts in to receiving automatic role rewards"
        this.guildOnly = true
        this.aliases = arrayOf("oi", "i")
        this.examples = arrayOf("optin", "oi", "i")
    }

    override fun execute(event: CommandEvent) {
        val guildId = event.guild.id

        val user = event.author

        RewardDAO.getInstance().optIn(guildId, user.id)

        logger.info("User ${user.name}#${user.discriminator} (${user.id}) opted in to rewards")
        event.reply("User ${NicknameUtils.determineDisplayName(event, user)} opted in to rewards")
    }
}