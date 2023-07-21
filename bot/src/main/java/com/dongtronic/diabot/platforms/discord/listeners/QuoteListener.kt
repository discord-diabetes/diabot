package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteAddCommand
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.dongtronic.diabot.submitMono
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandEvent
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import org.litote.kmongo.eq
import reactor.kotlin.core.publisher.toMono
import java.util.function.Consumer

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

    private suspend fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        if (event.retrieveUser().await().isBot) return
        if (!event.isFromGuild) return
        if (event.reaction.emoji !is UnicodeEmoji) return
        if (event.reaction.emoji != speechEmoji) return
        if (!QuoteDAO.checkRestrictions(event.guildChannel)) return

        val author = event.retrieveMember().await()
        val guild = event.guild

        /**
         * Replies to the channel only if the reacting user has permission to send messages
         */
        val reply: (() -> MessageCreateAction) -> Unit = { message ->
            if (event.guildChannel.canTalk(author)) {
                message().queue()
            }
        }

        val quoteMessage = Consumer<Message> { message ->
            QuoteDAO.getInstance().addQuote(
                    QuoteDTO(
                            guildId = guild.id,
                            channelId = event.guildChannel.id,
                            author = message.author.name,
                            authorId = message.author.id,
                            quoterId = event.userId,
                            message = message.contentRaw,
                            messageId = message.id
                    )
            ).subscribe({
                message.addReaction(speechEmoji).queue()
                reply { event.guildChannel.sendMessage(QuoteAddCommand.createAddedMessage(author.asMention, it.quoteId!!, message.jumpUrl)) }
            }, {
                reply { event.guildChannel.sendMessage("Could not create quote for message: ${message.id}") }
            })
        }

        QuoteDAO.getInstance()
                // search for any quotes with this message ID
                .getQuotes(guild.id, QuoteDTO::messageId eq event.messageId)
                .toMono()
                .subscribe({ /*ignored*/ }, { error ->
                    // only add quote if there are no quotes matching the reacted message ID
                    if (error is NoSuchElementException) {
                        event.retrieveMessage().submitMono()
                                .filter { (it.type == MessageType.DEFAULT || it.type == MessageType.INLINE_REPLY) && !it.author.isBot }
                                .subscribe(quoteMessage)
                    }
                })
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
