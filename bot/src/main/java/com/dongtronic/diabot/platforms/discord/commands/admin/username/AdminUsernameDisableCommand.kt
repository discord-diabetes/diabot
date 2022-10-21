package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.mongodb.NameRuleDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminUsernameDisableCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "disable"
        this.help = "Disable username pattern enforcement"
        this.guildOnly = true
        this.aliases = arrayOf("d")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        NameRuleDAO.instance.setEnforcing(event.guild.id, false).subscribe({
            event.reply("Disabled username enforcement for ${event.guild.name}")
        }, {
            logger.warn("Could not disable username enforcement for guild ${event.guild.id}", it)
            event.replyError("Could not disable username enforcement for ${event.guild.name}")
        })
    }
}
