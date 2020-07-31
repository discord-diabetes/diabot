package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class NightscoutAdminSimpleCommand(category: Command.Category, parent: DiscordCommand) : DiscordCommand(category, parent) {

    private val logger by Logger()

    init {
        this.name = "simple"
        this.help = "Simple-mode channel management"
        this.guildOnly = true
        this.aliases = arrayOf("sc", "shortchannels", "simplechannels")
        this.examples = arrayOf("diabot nsadmin sc add <channel>", "diabot nsadmin sc list", "diabot nsadmin sc delete <channel>")
        this.userPermissions = arrayOf(Permission.MANAGE_CHANNEL)
        this.children = arrayOf(
                NightscoutAdminSimpleListCommand(category, this),
                NightscoutAdminSimpleAddCommand(category, this),
                NightscoutAdminSimpleDeleteCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
