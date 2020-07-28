package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class QuoteEditCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val quoteRegex = Regex("(?<qid>.*) \"(?<message>[\\s\\S]*)\" ?- ?(?<author>.*[^\\s])")
    private val logger = LoggerFactory.getLogger(QuoteEditCommand::class.java)

    init {
        this.name = "edit"
        this.help = "Edits an existing quote by its ID"
        this.guildOnly = true
        this.aliases = arrayOf("e")
        this.examples = arrayOf(this.parent!!.name + " edit 1337 \"edited quote\" - edited author")
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        val match = quoteRegex.matchEntire(event.args)
        if (match == null) {
            event.replyError("Could not parse command. Please make sure you are using the correct format for this command")
            return
        }

        val id = match.groups["qid"]!!.value.trim()
        val oldQuote = QuoteDAO.getInstance().getQuote(event.guild.id, id)
        if (oldQuote == null) {
            event.replyError("No quote found for ID: $id")
            return
        }

        val author = match.groups["author"]!!.value.trim()
        val message = match.groups["message"]!!.value.trim()
        val dto = oldQuote.copy(author = author, message = message, messageId = event.message.idLong)

        QuoteDAO.getInstance().setQuote(event.guild.id, dto, false)
        event.replySuccess("Quote #$id edited")
    }
}