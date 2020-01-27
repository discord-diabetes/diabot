package com.dongtronic.diabot.platforms.discord.commands

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
        this.children = arrayOf(
                SampleListSubCommand(category, this),
                SampleAddSubCommand(category, this),
                SampleDeleteSubCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }

    class SampleListSubCommand(category: Category, parent: Command?) : DiabotCommand(category, parent) {
        private val logger = LoggerFactory.getLogger(SampleListSubCommand::class.java)

        init {
            this.name = "list"
            this.help = "an example list subcommand"
            this.guildOnly = false
            this.aliases = arrayOf("l")
        }

        override fun execute(event: CommandEvent?) {
            // do a thing
        }
    }

    class SampleAddSubCommand(category: Category, parent: Command?) : DiabotCommand(category, parent) {
        private val logger = LoggerFactory.getLogger(SampleAddSubCommand::class.java)

        init {
            this.name = "add"
            this.help = "an example add subcommand"
            this.guildOnly = false
            this.aliases = arrayOf("a")
        }

        override fun execute(event: CommandEvent?) {
            // do another thing
        }
    }

    class SampleDeleteSubCommand(category: Category, parent: Command?) : DiabotCommand(category, parent) {
        private val logger = LoggerFactory.getLogger(SampleDeleteSubCommand::class.java)

        init {
            this.name = "delete"
            this.help = "an example delete subcommand"
            this.guildOnly = false
            this.aliases = arrayOf("remove", "d", "r")
        }

        override fun execute(event: CommandEvent?) {
            // do the third thing
        }
    }
}
