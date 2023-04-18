package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.launch
import org.litote.kmongo.regex
import org.litote.kmongo.and
import org.bson.conversions.Bson
import net.dv8tion.jda.api.EmbedBuilder

class QuoteSearchCommand(category: Category, parent: QuoteCommand) : DiscordCommand(category, parent) {
    private val maxNumberOfQuotes: Int

    init {
        this.name = "search"
        this.help = "Searches through quotes for keywords (quotes contain *all* key words)"
        this.guildOnly = true
        this.aliases = arrayOf("s")
        this.examples = arrayOf(
            "diabot quote search some funny words",
            "diabot quote search r keyword"
        )

        this.maxNumberOfQuotes = System.getenv().getOrDefault("QUOTE_MAX_SEARCH_DISPLAY", "10").toInt()
    }

    override fun execute(event: CommandEvent) {
        runBlocking {
            launch {
                if (!QuoteDAO.awaitCheckRestrictions(event.guildChannel, warnDisabledGuild = true)) return@launch

                var args = event.message.contentRaw.split(" ").toList()
                if (args.size < 3) {
                    replyTooFewArgs(event)
                    return@launch
                }

                // Remove the command portion of the message
                args = args.slice(2 until args.size)

                // Do we want one random quote from our search?
                var random = false
                if (args[0] == "r") {
                    random = true
                    if (args.size < 2) {
                        replyTooFewArgs(event)
                        return@launch
                    }

                    args = args.slice(1 until args.size)
                }

                // Build a list of regex filters based on the provided keywords
                val filters: MutableList<Bson> = mutableListOf()
                for (arg in args) {
                    val keyword = Regex.escape(arg)
                    filters += QuoteDTO::message regex "(?i)$keyword"
                }

                if (random) {
                    // Get a single, random quote
                    val quote = QuoteDAO.getInstance().getRandomQuote(event.guild.id, and(filters)).block()
                    event.reply(createQuoteEmbed(quote))
                    return@launch
                }

                try {
                    val quotes = QuoteDAO.getInstance().getQuotes(event.guild.id, and(filters))

                    // Create an embed with up to 10 quotes as fields
                    val builder = EmbedBuilder()
                    val msg = event.message.contentRaw
                    builder.setAuthor("Diabot Quote Search")
                    builder.setDescription("Command: \"$msg\"")

                    val quotesIter = quotes.toIterable().iterator()
                    var count = 0
                    while (quotesIter.hasNext()) {
                        val quote = quotesIter.next()
                        if (count < maxNumberOfQuotes) {
                            // Ignore any quotes past the first 10
                            addQuoteToEmbed(builder, quote)
                        }
                        count += 1
                    }

                    if (count > maxNumberOfQuotes) {
                        builder.addField("Too many!", "$count quotes found, omitting the rest", false)
                    }

                    event.reply(builder.build())
                } catch (e: java.util.NoSuchElementException) {
                    event.replyError("Could not find any quote")
                }
            }
        }
    }

    private fun addQuoteToEmbed(builder: EmbedBuilder, quote: QuoteDTO) {
        builder.addField("#${quote.quoteId}", "\"${quote.message}\" - <@${quote.authorId}> (${quote.author})", false)
    }

    private fun replyTooFewArgs(event: CommandEvent) {
        event.replyError("Please specify at least one keyword to search for")
    }
}
