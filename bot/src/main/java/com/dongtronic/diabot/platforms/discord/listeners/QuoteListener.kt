package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.data.mongodb.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteAddCommand
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.dongtronic.diabot.submitMono
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.litote.kmongo.eq
import reactor.kotlin.core.publisher.toMono
import java.util.function.Consumer

class QuoteListener(private val client: CommandClient) : ListenerAdapter() {
    private val quoteCommand: QuoteCommand = client.commands.filterIsInstance(QuoteCommand::class.java).first()
    // https://emojiguide.org/speech-balloon
    private val speechEmoji = "U+1f4ac"
    private val logger = logger()

    override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
        if (event.user.isBot) return
        if (!event.reaction.reactionEmote.isEmoji) return
        if (event.reaction.reactionEmote.asCodepoints != speechEmoji) return
        if (!QuoteDAO.checkRestrictions(event.channel)) return

        val author = event.member
        val guild = event.guild

        /**
         * Replies to the channel only if the reacting user has permission to send messages
         */
        val reply: (() -> MessageAction) -> Unit = { message ->
            if (event.channel.canTalk(event.member)) {
                message().queue()
            }
        }

        val quoteMessage = Consumer<Message> { message ->
            QuoteDAO.getInstance().addQuote(QuoteDTO(
                    guildId = guild.id,
                    channelId = event.channel.id,
                    author = message.author.name,
                    authorId = message.author.id,
                    message = message.contentRaw,
                    messageId = message.id
            )).subscribe({
                message.addReaction(speechEmoji).queue()
                reply { event.channel.sendMessage(QuoteAddCommand.createAddedMessage(author.asMention, it.quoteId!!, message.jumpUrl)) }
            }, {
                reply { event.channel.sendMessage("Could not create quote for message: ${message.id}") }
            })
        }

        QuoteDAO.getInstance()
                // search for any quotes with this message ID
                .getQuotes(guild.id, QuoteDTO::messageId eq event.messageId)
                .toMono()
                .subscribe({/*ignored*/}, { error ->
                    // only add quote if there are no quotes matching the reacted message ID
                    if (error is NoSuchElementException) {
                        event.retrieveMessage().submitMono()
                                .filter { (it.type == MessageType.DEFAULT || it.type == MessageType.INLINE_REPLY) && !it.author.isBot }
                                .subscribe(quoteMessage)
                    }
                })
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isFromGuild) return

        val msg = event.message.contentRaw
        if (msg.startsWith(".")) {
            val fullCommand = msg.substringAfter('.')
            // split the command name from the arguments (if any)
            val cmd = fullCommand.split(' ', limit = 2)

            if (quoteCommand.isCommandFor(cmd[0])) {
                if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = false)) return

                val arguments = cmd.getOrNull(1) ?: ""
                val commandEvent = CommandEvent(event, ".", arguments, client)
                quoteCommand.run(commandEvent)
            }
        }
    }
}
