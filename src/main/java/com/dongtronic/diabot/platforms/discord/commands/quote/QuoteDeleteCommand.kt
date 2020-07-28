package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class QuoteDeleteCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val logger = LoggerFactory.getLogger(QuoteDeleteCommand::class.java)

    init {
        this.name = "delete"
        this.help = "Deletes a quote by its ID"
        this.guildOnly = true
        this.aliases = arrayOf("remove", "del", "d")
        this.examples = arrayOf(this.parent!!.name + " delete 1337")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (args.isEmpty() || args[0].isBlank()) {
            event.replyError("No quote ID specified")
            return
        }

        if (args[0].toLongOrNull() == null) {
            event.replyError("Quote ID must be numeric")
            return
        }

        if (QuoteDAO.getInstance().getQuote(event.guild.id, args[0]) != null) {
            QuoteDAO.getInstance().deleteQuote(event.guild.id, args[0])
            event.replySuccess("Quote #${args[0]} deleted")
        } else {
            event.replyError("No quote found for #${args[0]}")
        }
    }
}