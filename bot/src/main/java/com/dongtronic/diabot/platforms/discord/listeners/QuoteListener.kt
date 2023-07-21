package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteAddCommand
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import kotlinx.coroutines.reactive.awaitFirstOrNull
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import org.litote.kmongo.eq

class QuoteListener(private val client: CommandClient) : CoroutineEventListener {
    private val quoteCommand: QuoteCommand = client.commands.filterIsInstance(QuoteCommand::class.java).first()

    // https://emojiguide.org/speech-balloon
    private val speechEmoji = Emoji.fromUnicode("U+1f4ac")
    private val logger = logger()

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is MessageReactionAddEvent -> onMessageReactionAdd(event)
            is MessageReceivedEvent -> onMessageReceived(event)
        }
    }

    @Suppress("CyclomaticComplexMethod", "ReturnCount")
    private suspend fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (!event.isFromGuild) return
        if (event.reaction.emoji !is UnicodeEmoji || event.reaction.emoji != speechEmoji) return

        val reactor = event.retrieveMember().await()

        if (reactor.user.isBot) return
        if (!QuoteDAO.checkRestrictions(event.guildChannel)) return

        val message = event.retrieveMessage().await()

        if (message.author.isBot) return
        if (message.type != MessageType.DEFAULT && message.type != MessageType.INLINE_REPLY) return
        // Ignore event if the bot already created a quote from this message
        if (message.reactions.any { it.emoji == speechEmoji && it.isSelf }) return

        // Only add quote if there are no quotes with the message ID
        try {
            val quote = QuoteDAO.getInstance().getQuotes(event.guild.id, QuoteDTO::messageId eq event.messageId).awaitFirstOrNull()
            if (quote != null) return
        } catch (_: NoSuchElementException) {
        }

        val quote = try {
            QuoteDAO.getInstance().addQuote(
                    QuoteDTO(
                            guildId = event.guild.id,
                            channelId = event.guildChannel.id,
                            author = message.author.name,
                            authorId = message.author.id,
                            quoterId = reactor.id,
                            message = message.contentRaw,
                            messageId = message.id
                    )
            ).awaitFirstOrNull()
        } catch (_: Exception) {
            null
        }

        if (quote != null) {
            message.addReaction(speechEmoji).await()
        }

        // Don't reply if the reacting user doesn't have permission to send messages in the channel
        if (!event.guildChannel.canTalk(reactor)) return

        if (quote != null) {
            event.guildChannel.sendMessage(QuoteAddCommand.createAddedMessage(reactor.asMention, quote.quoteId!!, message.jumpUrl)).await()
        } else {
            event.guildChannel.sendMessage("Could not create quote for message: ${message.id}").await()
        }
    }

    private fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isFromGuild) return

        val msg = event.message.contentRaw
        if (msg.startsWith(".")) {
            val fullCommand = msg.substringAfter('.')
            // split the command name from the arguments (if any)
            val cmd = fullCommand.split(' ', limit = 2)

            if (quoteCommand.isCommandFor(cmd[0])) {
                if (!QuoteDAO.checkRestrictions(event.guildChannel, warnDisabledGuild = false)) return

                val arguments = cmd.getOrNull(1) ?: ""
                val commandEvent = CommandEvent(event, ".", arguments, client)
                quoteCommand.run(commandEvent)
            }
        }
    }
}
