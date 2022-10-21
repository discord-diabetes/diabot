package com.dongtronic.diabot.platforms.discord.commands.rewards

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent

class RewardsCommand(category: Category) : DiscordCommand(category, null) {

    private val logger = logger()

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
