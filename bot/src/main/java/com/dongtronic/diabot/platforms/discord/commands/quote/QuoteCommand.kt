package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.Message
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import reactor.core.publisher.Mono

class QuoteCommand(category: Category) : DiscordCommand(category, null) {
    private val logger = logger()

    init {
        this.name = "quote"
        this.help = "Gets saved quotes"
        this.guildOnly = true
        this.aliases = arrayOf("q")
        this.examples = arrayOf(
            "diabot quote",
            "diabot quote 1337",
            "diabot quote @Cas",
            "diabot quote 795873471530926100"
        )
        this.children = arrayOf(
            QuoteAddCommand(category, this),
            QuoteDeleteCommand(category, this),
            QuoteEditCommand(category, this),
            QuoteImportCommand(category, this),
            QuoteMineCommand(category, this),
            QuoteSearchCommand(category, this)
        )
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.guildChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val quote = if (args.isNotEmpty()) {
            val arg = args[0]
            val mentionId = Message.MentionType.USER.pattern.toRegex().find(arg)?.let {
                it.groups[1]?.value?.toLongOrNull()
            }

            if (mentionId != null) {
                getRandomQuote(event.guild.id, QuoteDTO::authorId eq mentionId.toString())
            } else if (arg.toLongOrNull() != null) {
                if (arg.length < 17) {
                    // the first discord snowflakes were 17 digits long
                    // assume it's a quote ID if it's less than 17 digits long
                    QuoteDAO.getInstance().getQuote(event.guild.id, arg)
                } else {
                    // assume it's a user ID otherwise
                    getRandomQuote(event.guild.id, QuoteDTO::authorId eq arg)
                }
            } else {
                // may be a username
                val joined = args.joinToString(" ")
                getRandomQuote(event.guild.id, QuoteDTO::author eq joined)
            }
        } else {
            getRandomQuote(event.guild.id)
        }

        quote.subscribe({
            event.reply(createQuoteEmbed(it))
        }, {
            event.replyError("Could not find any quote")
            if (it !is NoSuchElementException) {
                logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
            }
        })
    }

    /**
     * Gets a random quote fitting the given predicate, if any.
     *
     * @param guildId the guild ID to look under
     * @param filter filter for certain quotes
     * @return a random [QuoteDTO], or null if none found
     */
    private fun getRandomQuote(guildId: String, filter: Bson? = null): Mono<QuoteDTO> {
        return QuoteDAO.getInstance().getRandomQuote(guildId, filter)
    }
}
