package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.channels.AdminChannelAddCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.channels.AdminChannelDeleteCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.channels.AdminChannelListCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class AdminChannelsCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "channels"
        this.help = "Admin channel settings"
        this.guildOnly = true
        this.ownerCommand = false
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.aliases = arrayOf("c")
        this.examples = arrayOf("diabot admin channels add <channel>", "diabot admin channels delete <channel>", "diabot admin channels list")
        this.children = arrayOf(
            AdminChannelAddCommand(category, this),
            AdminChannelDeleteCommand(category, this),
            AdminChannelListCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
