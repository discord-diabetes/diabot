package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.util.Logger
import com.dongtronic.diabot.util.Patterns
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*

class OhNoListener : ListenerAdapter() {
    private val logger by Logger()

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.author.isBot) return

        val channel = event.channel

        val message = event.message.contentRaw

        val matcher = Patterns.ohNoPattern.matcher(message)

        if (!matcher.matches()) {
            return
        }

        val r = Random()

        val number = r.nextInt(100)

        if(number < 50) {
            return
        }

        channel.sendMessage("oh no").queue()


    }

}
