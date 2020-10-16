package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class NightscoutAdminCommand(category: Command.Category) : DiscordCommand(category, null) {

    private val logger = logger()

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
                NightscoutAdminSimpleCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val subcommands = children.joinToString(", ") { it.name }
        event.replyError("Valid sub-commands are: $subcommands")
    }
}