package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.rewards.*
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class AdminRewardsCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "rewards"
        this.help = "Rewards settings"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("r", "roles")
        this.examples = arrayOf("diabot admin rewards list", "diabot admin rewards add <required role> <reward role>")
        this.userPermissions = arrayOf(Permission.MANAGE_ROLES)
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
