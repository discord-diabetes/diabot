package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import java.time.Instant

class QuoteCommand(category: Category) : DiscordCommand(category, null) {
    private val mentionsRegex = Regex("<@(?<uid>\\d+)>")
    private val logger = LoggerFactory.getLogger(QuoteCommand::class.java)

    init {
        this.name = "quote"
        this.help = "Gets saved quotes"
        this.guildOnly = true
        this.aliases = arrayOf("q")
        this.examples = arrayOf("diabot quote", "diabot quote 1337", "diabot quote @Cas")
        this.children = arrayOf(
                QuoteAddCommand(category, this),
                QuoteDeleteCommand(category, this),
                QuoteEditCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val mentions = mentionsRegex.find(event.args)
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val quote = when {
            mentions != null -> {
                val name = resolveNameById(mentions.groups["uid"]!!.value, event)
                getRandomQuote(event.guild.id) { it.author.equals(name, ignoreCase = true) }
            }
            args.isNotEmpty() -> {
                if (args.all { it.toLongOrNull() != null }) {
                    // may be a quote id
                    QuoteDAO.getInstance().getQuote(event.guild.id, args[0])
                } else {
                    // may be a username
                    val joined = args.joinToString(" ")
                    getRandomQuote(event.guild.id) { it.author.equals(joined, ignoreCase = true) }
                }
            }
            else -> getRandomQuote(event.guild.id)
        }

        if (quote != null) {
            val messageStripped = stripMentions(quote.message, event)
            event.reply(createEmbed(quote.copy(message = messageStripped)))
        } else {
            event.replyError("Could not find any quote")
        }
    }

    /**
     * Creates an embed from a [QuoteDTO]
     *
     * @param quoteDTO the quote to generate an embed from
     * @return an embed containing the quote's details
     */
    private fun createEmbed(quoteDTO: QuoteDTO): MessageEmbed {
        val builder = EmbedBuilder()
        builder.setAuthor("#" + quoteDTO.id)

        val descriptionBuilder = StringBuilder(quoteDTO.message)
        descriptionBuilder.append("\n")
        descriptionBuilder.append("- ").append(quoteDTO.author)

        builder.setDescription(descriptionBuilder.toString())

        builder.setTimestamp(Instant.ofEpochSecond(quoteDTO.time))
        return builder.build()
    }

    /**
     * Gets a random quote fitting the given predicate, if any.
     *
     * @param guildId the guild ID to look under
     * @param predicate filter for certain quotes
     * @return a random [QuoteDTO], or null if none found
     */
    private fun getRandomQuote(guildId: String, predicate: (QuoteDTO) -> Boolean = { true }): QuoteDTO? {
        val quotes = QuoteDAO.getInstance().listQuotesByPredicate(guildId, predicate)
        if (quotes.isNullOrEmpty())
            return null
        return quotes.random()
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