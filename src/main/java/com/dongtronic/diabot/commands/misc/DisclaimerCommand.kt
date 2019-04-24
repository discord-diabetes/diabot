package com.dongtronic.diabot.commands.misc

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory
import java.io.File

class DisclaimerCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(DisclaimerCommand::class.java)

    init {
        this.name = "disclaimer"
        this.help = "Show the disclaimer for diabot"
        this.guildOnly = true
        this.ownerCommand = false
        this.hidden = false
    }

    override fun execute(event: CommandEvent) {
        val file = File("DISCLAIMER")
        val text = file.readText()

        event.reply(text)
    }
}
