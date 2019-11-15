package com.dongtronic.diabot.listener

import com.dongtronic.diabot.util.Patterns
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import java.util.*

class FeelListener : ListenerAdapter() {
    private val logger = LoggerFactory.getLogger(FeelListener::class.java)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return

        val channel = event.channel

        val message = event.message.contentRaw

        val matcher = Patterns.feelPattern.matcher(message)

        if (!matcher.matches()) {
            return
        }

        val r = Random()

        val number = r.nextInt(100)

        if(number < 95) {
            return
        }

        channel.sendMessage("https://youtu.be/fxn2A1oYqvs").queue()


    }

}
