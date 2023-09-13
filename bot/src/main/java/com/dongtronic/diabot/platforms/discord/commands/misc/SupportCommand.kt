package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class SupportCommand(category: Category) : DiscordCommand(category, null) {

    init {
        this.name = "support"
        this.help = "Get information about supporting Diabot development"
        this.hidden = false
        this.guildOnly = false
    }

    override fun execute(event: CommandEvent) {
        val builder = EmbedBuilder()

        builder.setTitle("Diabot Support")

        builder.setDescription("Diabot costs money to run and time to develop. You can help!")

        builder.addField("Contribute Code", "https://github.com/discord-diabetes/diabot", true)
        builder.addField("Donate money", "https://opencollective.com/diabot", true)

        builder.setColor(java.awt.Color.orange)

        event.reply(builder.build())
    }
}
