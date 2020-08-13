package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.AdminDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminUsernameDisableCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "disable"
        this.help = "Disable username pattern enforcement"
        this.guildOnly = true
        this.aliases = arrayOf("d")
    }

    override fun execute(event: CommandEvent) {
        AdminDAO.getInstance().setUsernameEnforcementEnabled(event.guild.id, false)

        event.reply("Disabled username enforcement for ${event.guild.name}")
    }
}
