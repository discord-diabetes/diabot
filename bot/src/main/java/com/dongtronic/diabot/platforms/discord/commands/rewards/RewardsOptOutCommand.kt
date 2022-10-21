package com.dongtronic.diabot.platforms.discord.commands.rewards

import com.dongtronic.diabot.data.mongodb.RewardsDAO
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class RewardsOptOutCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {

    private val logger = logger()

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
        RewardsDAO.instance.changeOpt(guildId, user.id, true).subscribe({
            logger.info("User ${user.name}#${user.discriminator} (${user.id}) opted out of rewards, $it")
            event.reply("User ${event.nameOf(user)} opted out of rewards")
        }, {
            logger.warn("Error while opting user ${user.name}#${user.discriminator} (${user.id}) out of rewards", it)
            event.replyError("Could not opt user ${event.nameOf(user)} out of rewards")
        })
    }
}
