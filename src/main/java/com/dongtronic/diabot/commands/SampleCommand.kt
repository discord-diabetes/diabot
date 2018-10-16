package com.dongtronic.diabot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory

class SampleCommand(category: Command.Category) : DiabotCommand() {

    private val logger = LoggerFactory.getLogger(SampleCommand::class.java)

    init {
        this.name = "test"
        this.help = "checks the bot's latency"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("tast", "tost")
        this.category = category
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {
        event.reply("You have Admin permission")
    }
}
