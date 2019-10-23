package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.commands.admin.rewards.*
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminRewardsCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminRewardsCommand::class.java)

    init {
        this.name = "rewards"
        this.help = "Rewards settings"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("r", "roles")
        this.examples = arrayOf("diabot admin rewards list", "diabot admin rewards add <required role ID> <reward role ID>")
        this.children = arrayOf(
                AdminRewardAddCommand(category, this),
                AdminRewardDeleteCommand(category, this),
                AdminRewardListCommand(category, this),
                AdminRewardOptinCommand(category, this),
                AdminRewardOptoutCommand(category, this),
                AdminRewardListOptoutsCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
