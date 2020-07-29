package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class QuoteListener : ListenerAdapter() {
    // https://emojiguide.org/speech-balloon
    private val speechEmoji = "U+1f4ac"
    private val logger = LoggerFactory.getLogger(QuoteListener::class.java)

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if (event.user.isBot) return
        if (event.reaction.reactionEmote.asCodepoints != speechEmoji) return
        if (!QuoteDAO.checkRestrictions(event.channel)) return

        val author = event.member
        val guild = event.guild

        event.channel.retrieveMessageById(event.messageId).queue { message ->
            QuoteDAO.getInstance().addQuote(QuoteDTO(
                    guildId = guild.idLong,
                    author = message.author.name,
                    authorId = message.author.idLong,
                    message = message.contentRaw,
                    messageId = message.idLong
            )).subscribe({
                message.addReaction(speechEmoji).queue()
                event.channel.sendMessage("New quote added by ${author.user.name} as #${it.quoteId}").queue()
            }, {
                event.channel.sendMessage("Could not create quote for message: ${message.id}").queue()
            })
        }
    }
}