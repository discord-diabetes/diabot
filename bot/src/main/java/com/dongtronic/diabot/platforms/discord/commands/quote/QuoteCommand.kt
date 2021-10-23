package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.bson.conversions.Bson
import org.litote.kmongo.eq
import reactor.core.publisher.Mono
import java.time.Instant

class QuoteCommand(category: Category) : DiscordCommand(category, null) {
    val mentionsRegex = Regex("<@!?(?<uid>\\d+)>")
    private val logger = logger()

    init {
        this.name = "quote"
        this.help = "Gets saved quotes"
        this.guildOnly = true
        this.aliases = arrayOf("q")
        this.examples = arrayOf("diabot quote", "diabot quote 1337", "diabot quote @Cas")
        this.children = arrayOf(
                QuoteAddCommand(category, this),
                QuoteDeleteCommand(category, this),
                QuoteEditCommand(category, this),
                QuoteImportCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val quote = when {
            event.message.mentionedMembers.isNotEmpty() -> {
                val member = event.message.mentionedMembers.first()
                getRandomQuote(event.guild.id, QuoteDTO::authorId eq member.id)
            }
            args.isNotEmpty() -> {
                if (args.all { it.toLongOrNull() != null }) {
                    // may be a quote id
                    QuoteDAO.getInstance().getQuote(event.guild.id, args[0])
                } else {
                    // may be a username
                    val joined = args.joinToString(" ")
                    getRandomQuote(event.guild.id, QuoteDTO::author eq joined)
                }
            }
            else -> getRandomQuote(event.guild.id)
        }

        quote.subscribe({
            event.reply(createEmbed(it))
        }, {
            event.replyError("Could not find any quote")
            if (it !is NoSuchElementException) {
                logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
            }
        })
    }

    /**
     * Creates an embed from a [QuoteDTO]
     *
     * @param quoteDTO the quote to generate an embed from
     * @return an embed containing the quote's details
     */
    private fun createEmbed(quoteDTO: QuoteDTO): MessageEmbed {
        val builder = EmbedBuilder()
        builder.setAuthor("#" + quoteDTO.quoteId)

        builder.setDescription(quoteDTO.message)

        addEmbedFooter(quoteDTO, builder)

        builder.setTimestamp(Instant.ofEpochSecond(quoteDTO.time))
        return builder.build()
    }

    /**
     * Adds the author name and message jump link to the end of a quote.
     * This function will also work around the 2048 char limit of embed descriptions if the new length is too long
     * by using embed fields instead of appending to the embed description.
     *
     * @param quoteDTO the quote to generate an embed from
     * @param builder the embed builder to add a quote footer to
     * @return the embed builder with a quote footer on it
     */
    private fun addEmbedFooter(quoteDTO: QuoteDTO, builder: EmbedBuilder): EmbedBuilder {
        val footer = StringBuilder("\n")
        footer.append("- ").append(quoteDTO.author)

        quoteDTO.getMessageLink()?.let { jumpLink ->
            val jumpText = "[(Jump)]($jumpLink)"
            footer.append(" ").append(jumpText)
        }

        if (footer.length + quoteDTO.message.length > MessageEmbed.TEXT_MAX_LENGTH) {
            // delete the newline char, it's not necessary when using a field
            footer.deleteCharAt(0)
            builder.addField("", footer.toString(), false)
        } else {
            builder.appendDescription(footer.toString())
        }

        return builder
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