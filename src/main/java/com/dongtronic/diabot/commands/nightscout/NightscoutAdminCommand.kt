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

class NightscoutAdminCommand(category: Command.Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminCommand::class.java)

    init {
        this.name = "nightscoutadmin"
        this.help = "Administrator commands for Nightscout"
        this.guildOnly = true
        this.aliases = arrayOf("nsadmin", "na")
        this.examples = arrayOf("diabot nsadmin list", "diabot nsadmin set <userId> <url>", "diabot nsadmin delete <userId>")
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.children = arrayOf(
                 NightscoutAdminSetCommand(category, this),
                NightscoutAdminDeleteCommand(category, this),
                NightscoutAdminShortCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}