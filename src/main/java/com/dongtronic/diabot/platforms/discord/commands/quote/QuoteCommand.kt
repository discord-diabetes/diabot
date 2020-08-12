package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
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
    val mentionsRegex = Regex("<@!(?<uid>\\d+)>")
    private val discordMessageLink = "https://discordapp.com/channels/{{guild}}/{{channel}}/{{message}}"
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
                getRandomQuote(event.guild.idLong, QuoteDTO::authorId eq member.idLong)
            }
            args.isNotEmpty() -> {
                if (args.all { it.toLongOrNull() != null }) {
                    // may be a quote id
                    QuoteDAO.getInstance().getQuote(event.guild.idLong, args[0].toLong())
                } else {
                    // may be a username
                    val joined = args.joinToString(" ")
                    getRandomQuote(event.guild.idLong, QuoteDTO::author eq joined)
                }
            }
            else -> getRandomQuote(event.guild.idLong)
        }

        quote.subscribe({
            val messageStripped = stripMentions(it.message, event)
            event.reply(createEmbed(it.copy(message = messageStripped)))
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

        val descriptionBuilder = StringBuilder(quoteDTO.message)
        descriptionBuilder.append("\n")
        descriptionBuilder.append("- ").append(quoteDTO.author)

        if (quoteDTO.guildId != 0L
                && quoteDTO.channelId != 0L
                && quoteDTO.messageId != 0L) {
            val jumpLink = discordMessageLink.replace("{{guild}}", quoteDTO.guildId.toString())
                    .replace("{{channel}}", quoteDTO.channelId.toString())
                    .replace("{{message}}", quoteDTO.messageId.toString())
            val jumpText = "[(Jump)]($jumpLink)"
            descriptionBuilder.append(" ").append(jumpText)
        }

        builder.setDescription(descriptionBuilder.toString())

        builder.setTimestamp(Instant.ofEpochSecond(quoteDTO.time))
        return builder.build()
    }

    /**
     * Gets a random quote fitting the given predicate, if any.
     *
     * @param guildId the guild ID to look under
     * @param filter filter for certain quotes
     * @return a random [QuoteDTO], or null if none found
     */
    private fun getRandomQuote(guildId: Long, filter: Bson? = null): Mono<QuoteDTO> {
        return QuoteDAO.getInstance().getRandomQuote(guildId, filter)
    }

    /**
     * Strips a message of mentions for users inside the guild.
     * Mentions which reference a user who is not in the guild will not be stripped.
     *
     * @param message the message to strip of mentions
     * @param event command event
     * @return stripped message
     */
    private fun stripMentions(message: String, event: CommandEvent): String {
        var strippedMessage = message
        val matches = mentionsRegex.findAll(message)
        // using `toMap` to remove duplicate mentions
        val uidMap = matches.mapNotNull {
            val uid = it.groups["uid"]?.value ?: return@mapNotNull null
            it.value to uid
        }.toMap()

        // replace the ids with their real names
        uidMap.mapValues { resolveNameById(it.value, event) }
                .forEach { (match, id) ->
                    if (id.isBlank())
                        return@forEach

                    strippedMessage = strippedMessage.replace(match, "@$id")
                }

        return strippedMessage
    }

    /**
     * Gets a nickname or account name by a Discord account ID if it is in the same guild
     *
     * @param id Discord account ID
     * @param event command event
     * @return account name if the id was found in the guild, otherwise blank
     */
    private fun resolveNameById(id: String, event: CommandEvent): String {
        return event.guild.getMemberById(id)?.effectiveName ?: ""
    }
}