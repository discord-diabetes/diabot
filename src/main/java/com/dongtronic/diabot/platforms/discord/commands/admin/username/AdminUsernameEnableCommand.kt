package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.AdminDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminUsernameEnableCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger by Logger()

    init {
        this.name = "enable"
        this.help = "Enable username pattern enforcement"
        this.guildOnly = true
        this.aliases = arrayOf("e")
    }

    override fun execute(event: CommandEvent) {
        AdminDAO.getInstance().setUsernameEnforcementEnabled(event.guild.id, true)

        event.reply("Enabled username enforcement for ${event.guild.name}")
    }
}
