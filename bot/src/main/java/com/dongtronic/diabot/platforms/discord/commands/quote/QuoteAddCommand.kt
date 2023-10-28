package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData

class QuoteAddCommand(category: Category, parent: QuoteCommand) : DiscordCommand(category, parent) {
    private val mentionsRegex = Regex("^<@!?(?<uid>\\d+)>\$")
    private val quoteRegex = Regex("\"(?<message>[\\s\\S]*)\" ?- ?(?<author>.*[^\\s])")
    private val logger = logger()

    init {
        this.name = "add"
        this.help = "Creates new quotes"
        this.guildOnly = true
        this.aliases = arrayOf("a")
        this.examples = arrayOf(this.parent!!.name + " add \"this is a quote added manually\" - gar")
    }

    override suspend fun executeSuspend(event: CommandEvent) {
        if (!QuoteDAO.awaitCheckRestrictions(event.guildChannel, warnDisabledGuild = true)) return

        val match = quoteRegex.find(event.message.contentRaw)
        if (match == null) {
            event.replyError("Could not parse quote. Please make sure you are using the correct format for this command")
            return
        }

        val message = match.groups["message"]!!.value.trim()
        var author = match.groups["author"]!!.value.trim()
        var authorId = 0L

        val mention = mentionsRegex.matchEntire(author)
        if (mention != null) {
            val uid = mention.groups["uid"]!!.value.trim().toLongOrNull()

            if (uid != null) {
                try {
                    val user = event.jda.retrieveUserById(uid).await()
                    author = user.name
                    authorId = uid
                } catch (ignored: Throwable) {
                }
            }
        }

        val quoteDto = QuoteDTO(
                guildId = event.guild.id,
                channelId = event.channel.id,
                author = author,
                authorId = authorId.toString(),
                quoterId = event.author.id,
                message = message,
                messageId = event.message.id
        )

        try {
            val quote = QuoteDAO.getInstance().addQuote(quoteDto).awaitSingle()
            event.reply(createAddedMessage(event.member.asMention, quote.quoteId!!))
        } catch (e: Throwable) {
            event.replyError("Could not add quote: ${e.message}")
            logger.warn("Unexpected error: " + e::class.simpleName + " - " + e.message)
        }
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
        fun createAddedMessage(quoterMention: String, quoteId: String, jumpUrl: String? = null): MessageCreateData {
            val msg = MessageCreateBuilder()
                    // mentions are used here solely for identifying who created the quote, so don't ping for it
                    .setAllowedMentions(emptyList())
                    .addContent("New quote added by $quoterMention as #$quoteId")

            if (jumpUrl != null) {
                msg.addContent(" (<$jumpUrl>)")
            }

            return msg.build()
        }
    }
}
