package com.dongtronic.diabot.commands.misc

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import java.time.temporal.ChronoUnit

class PingCommand(category: Command.Category) : DiabotCommand(category, null) {

    init {
        this.name = "ping"
        this.help = "checks the bot's latency"
        this.guildOnly = true
        this.aliases = arrayOf("pong")
        this.hidden = true
    }

    override fun execute(event: CommandEvent) {
        event.reply("Ping: ...") { m ->
            val ping = event.message.creationTime.until(m.creationTime, ChronoUnit.MILLIS)
            m.editMessage("Ping: " + ping + "ms | Websocket: " + event.jda.ping + "ms").queue()
        }
    }
}
