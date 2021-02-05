package com.dongtronic.diabot.platforms.discord.listeners

import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*

/**
 * Listener for when an author deletes their command execution message, which causes a deletion of any replies to the
 * author's message.
 *
 * @param maxReplies The maximum number of replies to track in history. Once the number of replies exceeds this value,
 * the oldest replies will no longer be tracked for deletion.
 */
class CommandUpdateListener(maxReplies: Int) : ListenerAdapter() {
    private val responses = limitedMap<Long, Array<Long>>(maxReplies)

    override fun onMessageDelete(event: MessageDeleteEvent) {
        val responseIds = responses[event.messageIdLong]

        if (responseIds != null) {
            responseIds.forEach { responseId ->
                event.channel.deleteMessageById(responseId).queue()
            }
            responses.remove(event.messageIdLong)
        }
    }

    /**
     * Log a command response message.
     *
     * @param executingId The message ID of the author's message (that executed the command to respond)
     * @param responseId The message ID of the command's response to the author
     */
    fun markReply(executingId: Long, responseId: Long) {
        responses.merge(executingId, arrayOf(responseId)) { old: Array<Long>, new: Array<Long> ->
            old.plus(new)
        }
    }

    /**
     * Makes a limited-capacity [LinkedHashMap]. Removal follows FIFO, meaning the oldest entries will be removed first.
     *
     * @param maxEntries The maximum number of entries to hold in the map before removing old ones
     * @return A [LinkedHashMap] whose size will never exceed [maxEntries]
     */
    private fun <K, V> limitedMap(maxEntries: Int): LinkedHashMap<K, V> {
        return object : LinkedHashMap<K, V>(maxEntries, 1f, false) {
            override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
                return size > maxEntries
            }
        }
    }
}