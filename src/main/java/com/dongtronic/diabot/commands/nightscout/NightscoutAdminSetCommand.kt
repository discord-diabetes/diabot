package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class NightscoutAdminSetCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminSetCommand::class.java)

    init {
        this.name = "set"
        this.help = "Set Nightscout URL for a user"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("s")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " set <userId> <url>")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
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
