package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.username.AdminUsernameDisableCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.username.AdminUsernameEnableCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.username.AdminUsernameHintCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.username.AdminUsernamePatternCommand
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
        this.children = arrayOf(
                AdminUsernamePatternCommand(category, this),
                AdminUsernameEnableCommand(category, this),
                AdminUsernameDisableCommand(category, this),
                AdminUsernameHintCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
