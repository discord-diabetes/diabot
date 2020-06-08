package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.channels.AdminChannelAddCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.channels.AdminChannelDeleteCommand
import com.dongtronic.diabot.platforms.discord.commands.admin.channels.AdminChannelListCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import org.slf4j.LoggerFactory

class AdminChannelsCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminChannelsCommand::class.java)

    init {
        this.name = "channels"
        this.help = "Admin channel settings"
        this.guildOnly = true
        this.ownerCommand = false
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.aliases = arrayOf("c")
        this.examples = arrayOf("diabot admin channels add <channelId>", "diabot admin channels delete <channelId>", "diabot admin channels list")
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
