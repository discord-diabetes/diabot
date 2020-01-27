package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.logic.`fun`.ExcuseGetter
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.jsoup.Jsoup

import java.io.IOException

class ExcuseCommand(category: Command.Category) : Command() {

    init {
        this.name = "excuse"
        this.help = "gibs excus"
        this.category = category
    }

    override fun execute(event: CommandEvent) {
        try {
            event.reply(ExcuseGetter.get())
        } catch (e: IOException) {
            event.replyError("Oops")
        }

    }
}
