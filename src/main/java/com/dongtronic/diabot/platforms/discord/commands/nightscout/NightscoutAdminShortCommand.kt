package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import org.slf4j.LoggerFactory

class NightscoutAdminShortCommand(category: Command.Category, parent: DiabotCommand) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminShortCommand::class.java)

    init {
        this.name = "simple"
        this.help = "Simple-mode channel management"
        this.guildOnly = true
        this.aliases = arrayOf("sc", "shortchannels", "simplechannels")
        this.examples = arrayOf("diabot nsadmin sc add <channel>", "diabot nsadmin sc list", "diabot nsadmin sc delete <channel>")
        this.userPermissions = arrayOf(Permission.MANAGE_CHANNEL)
        this.children = arrayOf(
                NightscoutAdminShortListCommand(category, this),
                NightscoutAdminShortAddCommand(category, this),
                NightscoutAdminShortDeleteCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}
