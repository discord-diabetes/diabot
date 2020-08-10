package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.data.QuoteDAO
import com.dongtronic.diabot.data.QuoteDTO
import com.dongtronic.diabot.platforms.discord.commands.quote.QuoteCommand
import com.jagrosh.jdautilities.command.CommandClient
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.litote.kmongo.eq
import org.slf4j.LoggerFactory
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.function.Consumer

class QuoteListener(private val client: CommandClient) : ListenerAdapter() {
    private val quoteCommand: QuoteCommand = client.commands.filterIsInstance(QuoteCommand::class.java).first()
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

        /**
         * Replies to the channel only if the reacting user has permission to send messages
         */
        val reply: (message: String) -> Unit = {
            if (event.channel.canTalk(event.member)) {
                event.channel.sendMessage(it).queue()
            }
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
                reply("New quote added by ${author.effectiveName} as #${it.quoteId}")
            }, {
                reply("Could not create quote for message: ${message.id}")
            })
        }

        QuoteDAO.getInstance()
                // search for any quotes with this message ID
                .getQuotes(guild.idLong, QuoteDTO::messageId eq event.messageIdLong)
                .toMono()
                .subscribe({/*ignored*/}, { error ->
                    // if there are no quotes then proceed
                    if (error is NoSuchElementException) {
                        messageRetrieval
                                .filter { it.type == MessageType.DEFAULT && !it.author.isBot }
                                .subscribe(quoteMessage)
                    }
                })
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.isFromGuild) return

        val msg = event.message.contentStripped
        if (msg.startsWith(".")) {
            val fullCommand = msg.substringAfter('.')
            // split the command name from the arguments (if any)
            val cmd = fullCommand.split(' ', limit = 2)

            if (quoteCommand.isCommandFor(cmd[0])) {
                if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = false)) return

                val arguments = cmd.getOrNull(1) ?: ""
                val commandEvent = CommandEvent(event, arguments, client)
                quoteCommand.run(commandEvent)
            }
        }
    }
}