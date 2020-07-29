package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.litote.kmongo.eq
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.Consumer

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

        val messageRetrieval = Mono.defer {
            // defer to prevent retrieving message until subscribe
            event.channel.retrieveMessageById(event.messageId)
                .submit().toMono()
        }

        val quoteMessage = Consumer<Message> { message ->
            QuoteDAO.getInstance().addQuote(QuoteDTO(
                    guildId = guild.idLong,
                    channelId = event.channel.idLong,
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

        QuoteDAO.getInstance()
                // search for any quotes with this message ID
                .getQuotes(guild.idLong, QuoteDTO::messageId eq event.messageIdLong)
                .toMono()
                .subscribe({/*ignored*/}, {
                    if (it is NoSuchElementException) {
                        // if there is no quotes then proceed
                        messageRetrieval.subscribe(quoteMessage)
                    }
                })
    }
}