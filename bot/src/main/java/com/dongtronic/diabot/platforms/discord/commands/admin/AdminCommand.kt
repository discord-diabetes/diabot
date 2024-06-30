package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class AdminCommand(category: Category) : DiscordCommand(category, null) {

    init {
        this.name = "admin"
        this.help = "Administrator commands"
        this.guildOnly = true
        this.aliases = arrayOf("a")
        this.examples = arrayOf()
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.children = arrayOf(
            AdminUsernameCommand(category, this),
            AdminRewardsCommand(category, this),
            AdminChannelsCommand(category, this),
            AdminAnnounceCommand(category, this),
        )
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
