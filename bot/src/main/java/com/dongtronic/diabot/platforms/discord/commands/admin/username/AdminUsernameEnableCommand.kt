package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.mongodb.NameRuleDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminUsernameEnableCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "enable"
        this.help = "Enable username pattern enforcement"
        this.guildOnly = true
        this.aliases = arrayOf("e")
    }

    override fun execute(event: CommandEvent) {
        NameRuleDAO.instance.setEnforcing(event.guild.id, true).subscribe({
            event.reply("Enabled username enforcement for ${event.guild.name}")
        }, {
            logger.warn("Could not enable username enforcement for guild ${event.guild.id}", it)
            event.replyError("Could not enable username enforcement for ${event.guild.name}")
        })
    }
}
