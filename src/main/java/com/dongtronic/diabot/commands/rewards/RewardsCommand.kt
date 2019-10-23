package com.dongtronic.diabot.commands.rewards

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class RewardsCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(RewardsCommand::class.java)

    init {
        this.name = "rewards"
        this.help = "Opt in or out of automatic role rewards"
        this.guildOnly = true
        this.aliases = arrayOf("reward", "r")
        this.children = arrayOf(
                RewardsOptInCommand(category, this),
                RewardsOptOutCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
