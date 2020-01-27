package com.dongtronic.diabot.platforms.discord.commands.rewards

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.RewardDAO
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class RewardsOptOutCommand(category: Category, parent: Command) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(RewardsOptOutCommand::class.java)

    init {
        this.name = "optout"
        this.help = "Opts out of receiving automatic role rewards"
        this.guildOnly = true
        this.aliases = arrayOf("ou", "o")
        this.examples = arrayOf("optout", "ou", "o")
    }

    override fun execute(event: CommandEvent) {
        val guildId = event.guild.id

        val user = event.author

        RewardDAO.getInstance().optOut(guildId, user.id)

        logger.info("User ${user.name}#${user.discriminator} (${user.id}) opted out of rewards")
        event.reply("User ${NicknameUtils.determineDisplayName(event, user)} opted out of rewards")
    }
}