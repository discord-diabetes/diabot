package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.authorName
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class NightscoutAdminSetCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

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

            val user = if (event.message.mentions.users.size == 0) {
                if (!StringUtils.isNumeric(args[0])) {
                    throw IllegalArgumentException("User ID must be valid")
                }

                val userId = args[0]
                // this can be left unisolated because this is an owner-only command
                event.jda.getUserById(userId)
                    ?: throw IllegalArgumentException("User `$userId` is not in the server")
            } else {
                event.message.mentions.users[0]
            }

            logger.info("Admin setting URL for user ${args[0]} to ${args[1]}")

            NightscoutFacade.setUrl(user, args[1]).subscribe({
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
