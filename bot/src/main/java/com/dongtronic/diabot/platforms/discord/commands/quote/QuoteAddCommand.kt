package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message

class QuoteAddCommand(category: Category, parent: QuoteCommand) : DiscordCommand(category, parent) {
    private val mentionsRegex = parent.mentionsRegex
    private val quoteRegex = Regex("\"(?<message>[\\s\\S]*)\" ?- ?(?<author>.*[^\\s])")
    private val logger = logger()

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

        val quoteDto = QuoteDTO(guildId = event.guild.id,
                channelId = event.channel.id,
                author = author,
                authorId = authorId.toString(),
                message = message,
                messageId = event.message.id)

        QuoteDAO.getInstance().addQuote(quoteDto).subscribe(
                {
                    event.reply(createAddedMessage(event.member.asMention, it.quoteId!!))
                },
                {
                    event.replyError("Could not add quote: ${it.message}")
                    logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
                })
    }

    companion object {
        /**
         * Build a message for when a quote is created.
         *
         * @param quoterMention Quoter user as a mention
         * @param quoteId ID of the created quote
         * @param jumpUrl Optional jump URL to the quoted message
         * @return A message indicating that a quote was created
         */
        fun createAddedMessage(quoterMention: String, quoteId: String, jumpUrl: String? = null): Message {
            val msg = MessageBuilder()
                    // mentions are used here solely for identifying who created the quote, so don't ping for it
                    .denyMentions(Message.MentionType.USER)
                    .append("New quote added by $quoterMention as #$quoteId")

            if (jumpUrl != null)
                msg.append(" (<$jumpUrl>)")

            return msg.build()
        }
    }
}