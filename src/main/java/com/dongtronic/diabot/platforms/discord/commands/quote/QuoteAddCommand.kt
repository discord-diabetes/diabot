package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class QuoteAddCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val quoteRegex = Regex("\"(?<message>[\\s\\S]*)\" ?- ?(?<author>.*[^\\s])")
    private val logger = LoggerFactory.getLogger(QuoteAddCommand::class.java)

    init {
        this.name = "add"
        this.help = "Creates new quotes"
        this.guildOnly = true
        this.aliases = arrayOf("a")
        this.examples = arrayOf(this.parent!!.name + " add \"this is a quote added manually\" - gar")
    }

    override fun execute(event: CommandEvent) {
        val match = quoteRegex.matchEntire(event.args)
        if (match == null) {
            event.replyError("Could not parse quote. Please make sure you are using the correct format for this command")
            return
        }
        val author = match.groups["author"]!!.value.trim()
        val message = match.groups["message"]!!.value.trim()

        val quote = QuoteDAO.getInstance().addQuote(
                guildId = event.guild.id,
                quote = QuoteDTO(author = author, message = message, messageId = event.message.idLong)
        )
        if (quote != null) {
            event.replySuccess("New quote added by ${event.author.name} as #${quote.id}")
        } else {
            event.replyError("Could not add quote")
        }
    }
}