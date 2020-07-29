package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission
import org.slf4j.LoggerFactory

class QuoteDeleteCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val logger = LoggerFactory.getLogger(QuoteDeleteCommand::class.java)

    init {
        this.name = "delete"
        this.help = "Deletes a quote by its ID"
        this.guildOnly = true
        this.aliases = arrayOf("remove", "del", "d", "rm")
        this.userPermissions = arrayOf(Permission.MESSAGE_MANAGE)
        this.examples = arrayOf(this.parent!!.name + " delete 1337")
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (args.isEmpty() || args[0].isBlank()) {
            event.replyError("No quote ID specified")
            return
        }
        val quoteId = args[0].toLongOrNull()
        if (quoteId == null) {
            event.replyError("Quote ID must be numeric")
            return
        }

        QuoteDAO.getInstance().deleteQuote(event.guild.idLong, quoteId).subscribe({
            if (it.wasAcknowledged()) {
                event.replySuccess("Quote #$quoteId deleted")
            } else {
                event.replyError("No quote found for #$quoteId")
            }
        }, {
            event.replyError("Could not delete quote")
            logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
        })
    }
}