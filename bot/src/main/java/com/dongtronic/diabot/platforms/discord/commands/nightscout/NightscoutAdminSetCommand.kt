package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.authorName
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.commands.nightscout.NightscoutSetUrlCommand.Companion.validateNightscoutUrl
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class NightscoutAdminSetCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "set"
        this.help = "Set Nightscout URL for a user"
        this.guildOnly = true
        this.ownerCommand = true
        this.aliases = arrayOf("s")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " set <userId> <url>")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // This command may exclusively be run on the official Diabetes server.
        if (event.guild.id != System.getenv("HOME_GUILD_ID")) {
            event.replyError(System.getenv("HOME_GUILD_MESSAGE"))
            return
        }

        try {
            if (args.size != 2) {
                throw IllegalArgumentException("Please provide the <userId> and <url> parameters")
            }

            if (!StringUtils.isNumeric(args[0])) {
                throw IllegalArgumentException("User ID must be numeric")
            }

            val url = validateNightscoutUrl(args[1])

            val user = event.jda.getUserById(args[0])
                    ?: throw IllegalArgumentException("User `${args[0]}` does not exist in this server")

            logger.info("Admin setting URL for user ${args[0]} to ${args[1]}")

            NightscoutDAO.instance.setUrl(user.id, url).subscribe({
                event.reply("Admin set Nightscout URL for ${event.nameOf(user)} [requested by ${event.authorName}]")
            }, {
                val msg = "Could not set Nightscout URL for ${event.nameOf(user)} (${user.id})"
                logger.warn(msg, it)
                event.replyError(msg)
            })
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }
}
