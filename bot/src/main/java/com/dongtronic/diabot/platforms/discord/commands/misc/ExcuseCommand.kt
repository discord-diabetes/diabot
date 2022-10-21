package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.logic.`fun`.ExcuseGetter
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import java.io.IOException

class ExcuseCommand(category: Category) : Command() {

    init {
        this.name = "excuse"
        this.help = "gibs excus"
        this.category = category
        this.guildOnly = false
    }

    override fun execute(event: CommandEvent) {
        try {
            event.reply(ExcuseGetter.get())
        } catch (e: IOException) {
            logger().warn("Unexpected error while getting excuse", e)
            event.replyError("Oops")
        }
    }
}
