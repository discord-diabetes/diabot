package com.dongtronic.diabot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class SampleSubCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(SampleSubCommand::class.java)

    init {
        this.name = "sub"
        this.help = "an example sub command"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("s")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " sub")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            event.replyError("must include operation")
            return
        }

        val command = args[0].toUpperCase()

        try {
            when (command) {
                "LIST", "L" -> someAction(event)
                "ADD", "A" -> anotherAction(event)
                "DELETE", "REMOVE", "D", "R" -> thirdAction(event)
                else -> {
                    throw IllegalArgumentException("unknown command $command")
                }
            }

        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }

    private fun someAction(event: CommandEvent) {
        // do a thing
    }

    private fun anotherAction(event: CommandEvent) {
        // do another thing
    }

    private fun thirdAction(event: CommandEvent) {
        // do the third thing
    }
}
