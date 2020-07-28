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

        val author = event.member
        val guild = event.guild

        event.channel.retrieveMessageById(event.messageId).queue {
            val quote = QuoteDAO.getInstance().addQuote(guild.id, QuoteDTO(
                    author = it.author.name,
                    authorId = it.author.idLong,
                    message = it.contentRaw,
                    messageId = it.idLong
            ))

            if (quote != null) {
                it.addReaction(speechEmoji).queue()
                event.channel.sendMessage("New quote added by ${author.user.name} as #${quote.id}").queue()
            } else {
                event.channel.sendMessage("Could not create quote for message: ${it.id}").queue()
            }
        }
    }
}