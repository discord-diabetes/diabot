package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class NightscoutAdminShortCommand(category: Command.Category, parent: DiabotCommand) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminShortCommand::class.java)

    init {
        this.name = "short"
        this.help = "Short channel management"
        this.guildOnly = true
        this.aliases = arrayOf("sc", "shortchannels")
        this.examples = arrayOf("diabot nsadmin sc add <channel>", "diabot nsadmin sc list", "diabot nsadmin sc delete <channel>")
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
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
