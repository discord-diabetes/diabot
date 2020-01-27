package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminUsernameEnableCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminUsernameEnableCommand::class.java)

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
