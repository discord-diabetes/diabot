package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDTO
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.EmbedBuilder
import java.time.Instant

/**
 * Creates an embed from a [QuoteDTO]
 *
 * @param quoteDTO the quote to generate an embed from
 * @return an embed containing the quote's details
 */
fun createQuoteEmbed(quoteDTO: QuoteDTO): MessageEmbed {
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
fun addEmbedFooter(quoteDTO: QuoteDTO, builder: EmbedBuilder): EmbedBuilder {
    val footer = StringBuilder("\n")
    if (quoteDTO.authorId != "0") {
        // adding the author name in parentheses is to work around the @invalid-user bug on mobile
        footer.append("- <@${quoteDTO.authorId}> (${quoteDTO.author})")
    } else {
        footer.append("- ").append(quoteDTO.author)
    }

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
