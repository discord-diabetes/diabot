package com.dongtronic.diabot.platforms.discord.commands.rewards

import com.dongtronic.diabot.data.redis.RewardDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class RewardsOptInCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {

    private val logger = logger()

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