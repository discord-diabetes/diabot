package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class QuoteAddCommand(category: Category, parent: QuoteCommand) : DiscordCommand(category, parent) {
    private val mentionsRegex = parent.mentionsRegex
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
        if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = true)) return

        val match = quoteRegex.find(event.message.contentRaw)
        if (match == null) {
            event.replyError("Could not parse quote. Please make sure you are using the correct format for this command")
            return
        }

        var authorId = 0L
        var author = match.groups["author"]!!.value.trim()
        val message = match.groups["message"]!!.value.trim()

        val mention = mentionsRegex.matchEntire(author)
        if (mention != null) {
            val mentioned = event.message.mentionedMembers.lastOrNull()
            val uid = mention.groups["uid"]!!.value.trim().toLongOrNull()
            if (mentioned != null && uid != null) {
                author = mentioned.user.name
                authorId = uid
            }
        }

        val quoteDto = QuoteDTO(guildId = event.guild.idLong,
                channelId = event.channel.idLong,
                author = author,
                authorId = authorId,
                message = message,
                messageId = event.message.idLong)

        QuoteDAO.getInstance().addQuote(quoteDto).subscribe(
                {
                    event.replySuccess("New quote added by ${event.member.effectiveName} as #${it.quoteId}")
                },
                {
                    event.replyError("Could not add quote: ${it.message}")
                    logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
                })
    }
}