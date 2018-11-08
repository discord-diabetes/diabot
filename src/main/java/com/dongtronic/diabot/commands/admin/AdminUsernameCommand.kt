package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminUsernameCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminUsernameCommand::class.java)

    init {
        this.name = "usernames"
        this.help = "Username rule enforcement"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("u")
    }

    override fun execute(event: CommandEvent) {
        event.reply("You have Admin permission")
    }
}
