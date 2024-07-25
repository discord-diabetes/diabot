package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.litote.kmongo.eq

class QuoteMineCommand(category: Category, parent: QuoteCommand) : DiscordCommand(category, parent) {
    private val logger = logger()

    init {
        this.name = "mine"
        this.help = "Lists my quotes"
        this.guildOnly = true
        this.aliases = arrayOf("m")
        this.examples = arrayOf(this.parent!!.name + " mine")
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.guildChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        QuoteDAO.getInstance().getQuotes(event.guild.id, QuoteDTO::authorId eq event.member.id)
            .collectList().subscribe(
                {
                    event.reply(createEmbed(event.author.name, it))
                }, {
                    logger.info("Error finding all quotes for " + event.author.name)
                })
    }

    private fun createEmbed(author: String, quoteDTOs: List<QuoteDTO>): MessageEmbed {
        val builder = EmbedBuilder()

        val quoteIdList = quoteDTOs.map { "#" + it.quoteId + ": " + it.message }
        val joinedQuoteIds = quoteIdList.joinToString("\n")

        builder.setAuthor("$author's Quotes")
        builder.setDescription(joinedQuoteIds.ifEmpty { "User has no quotes!" })

        return builder.build()
    }
}
