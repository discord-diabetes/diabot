package com.dongtronic.diabot.platforms.discord.commands

import cloud.commandframework.jda.JDACommandSender
import com.dongtronic.diabot.commands.CommandUser
import com.dongtronic.diabot.commands.ReplyType
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.listeners.CommandUpdateListener
import com.dongtronic.diabot.submitMono
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import reactor.core.publisher.Mono

class JDACommandUser(
        event: MessageReceivedEvent,
        mapper: ResponseEmojiMapper = ResponseEmojiMapper(),
        private val listener: CommandUpdateListener? = null
) : CommandUser<MessageReceivedEvent, Message>(event, mapper) {
    override fun getAuthorName(): String {
        return event.author.name
    }

    override fun getAuthorDisplayName(): String {
        return event.nameOf(event.author)
    }

    override fun getAuthorUniqueId(): String {
        return event.author.id
    }

    override fun getAuthorMention(): String {
        return event.author.asMention
    }

    override fun deleteAuthorMessage(reason: String?): Mono<Boolean> {
        return event.message.delete()
                .reason(reason)
                .submitMono()
                .subscribeOn(defaultScheduler)
                .map { true }
    }

    override fun reply(message: Message, type: ReplyType): Mono<Message> {
        val action = when (type) {
            ReplyType.NATIVE_REPLY -> event.message.reply(message)
            ReplyType.NONE -> event.channel.sendMessage(message)
            ReplyType.MENTION -> {
                // insert mention at beginning
                val messageBuilder = MessageBuilder(message)
                messageBuilder.stringBuilder.insert(0, getAuthorMention() + ' ')
                event.channel.sendMessage(messageBuilder.build())
            }
        }

        return action.submitMono().subscribeOn(defaultScheduler)
                .doOnNext { listener?.markReply(event.message.idLong, it.idLong) }
    }

    override fun reply(message: CharSequence, type: ReplyType): Mono<Message> {
        return reply(MessageBuilder(message).build(), type)
    }

    /**
     * Replies to the author's message with an embed.
     *
     * @param embed Message embed to send
     * @param type Method of replying
     * @return A [Mono] of the sent message
     */
    fun reply(embed: MessageEmbed, type: ReplyType): Mono<Message> {
        return reply(MessageBuilder(embed).build(), type)
    }

    override fun react(unicode: String): Mono<Boolean> {
        return event.message
                .addReaction(unicode)
                .submitMono()
                .subscribeOn(defaultScheduler)
                .map { true }
    }

    /**
     * Converts this [JDACommandUser] object (Diabot) into a [JDACommandSender] object (cloud)
     *
     * @return Converted [JDACommandSender]
     */
    fun toJdaCommandSender(): JDACommandSender {
        return JDACommandSender.of(event)
    }

    companion object {
        private val emojiMapper = ResponseEmojiMapper()

        /**
         * Converts a [JDACommandSender] object (cloud) into a [JDACommandUser] object (Diabot)
         *
         * @param jdaCommandSender [JDACommandSender] to convert
         * @param listener An optional [CommandUpdateListener] to delete replies when an author deletes their message
         * @return Converted [JDACommandUser]
         */
        fun of(jdaCommandSender: JDACommandSender, listener: CommandUpdateListener? = null): JDACommandUser {
            return JDACommandUser(jdaCommandSender.event.get(), emojiMapper, listener)
        }
    }
}