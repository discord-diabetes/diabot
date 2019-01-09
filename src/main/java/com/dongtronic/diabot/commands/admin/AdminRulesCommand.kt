package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.commands.admin.username.AdminUsernameDisableCommand
import com.dongtronic.diabot.commands.admin.username.AdminUsernameEnableCommand
import com.dongtronic.diabot.commands.admin.username.AdminUsernameHintCommand
import com.dongtronic.diabot.commands.admin.username.AdminUsernamePatternCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminRulesCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminRulesCommand::class.java)

    init {
        this.name = "rules"
        this.help = "Rule management"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("r")
        this.children = arrayOf(
                AdminUsernamePatternCommand(category, this),
                AdminUsernameEnableCommand(category, this),
                AdminUsernameDisableCommand(category, this),
                AdminUsernameHintCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        event.replyError("Unknown command: ${args[0]}")
    }
}
