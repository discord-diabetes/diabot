package com.dongtronic.diabot.commands.admin.username

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminUsernameDisableCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminUsernameDisableCommand::class.java)

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
