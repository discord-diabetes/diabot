package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.logic.`fun`.Awyisser
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import java.awt.Color

class AwyissCommand(category: Command.Category) : DiscordCommand(category, null) {

    init {
        this.name = "awyiss"
        this.help = "muther f'in breadcrumbs"
        this.arguments = "<phrase> ..."
        this.guildOnly = false
        this.aliases = arrayOf("duck", "breadcrumbs")
    }

    override fun execute(event: CommandEvent) {
        event.reactSuccess()

        try {
            val imageUrl = Awyisser.generate(event.args)
            val builder = EmbedBuilder()

            builder.setTitle("Awyiss - " + event.args)
            builder.setAuthor(event.author.name)
            builder.setImage(imageUrl)
            builder.setColor(Color.white)

            val embed = builder.build()

            event.reply(embed)
        } catch (e: Exception) {
            event.replyError("Something went wrong: " + e.message)
        }
    }
}
