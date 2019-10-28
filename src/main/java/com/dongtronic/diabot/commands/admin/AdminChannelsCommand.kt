package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.commands.admin.channels.AdminChannelAddCommand
import com.dongtronic.diabot.commands.admin.channels.AdminChannelDeleteCommand
import com.dongtronic.diabot.commands.admin.channels.AdminChannelListCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminChannelsCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminChannelsCommand::class.java)

    init {
        this.name = "channels"
        this.help = "Admin channel settings"
        this.guildOnly = true
        this.ownerCommand = false
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
