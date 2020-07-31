package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class NightscoutAdminSetCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger by Logger()

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

            NightscoutDAO.getInstance().setNightscoutUrl(user, url)

            event.reply("Admin set Nightscout URL for ${user.discriminator} [requested by ${event.author.name}]")
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }

    private fun validateNightscoutUrl(url: String): String {
        var finalUrl = url
        if (!finalUrl.contains("http://") && !finalUrl.contains("https://")) {
            throw IllegalArgumentException("Url must contain scheme")
        }

        if (finalUrl.endsWith("/")) {
            finalUrl = finalUrl.trimEnd('/')
        }

        return finalUrl
    }
}
