package com.dongtronic.diabot.platforms.discord.commands.rewards

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
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

        RewardsDAO.instance.changeOpt(guildId, user.id, false).subscribe({
            logger.info("User ${user.name}#${user.discriminator} (${user.id}) opted in to rewards, $it")
            event.reply("User ${event.nameOf(user)} opted in to rewards")
        }, {
            logger.warn("Error while opting user ${user.name}#${user.discriminator} (${user.id}) in to rewards", it)
            event.replyError("Could not opt user ${event.nameOf(user)} in to rewards")
        })
    }
}