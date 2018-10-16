package com.dongtronic.diabot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.awt.Color

class ReplyCommand(category: Command.Category) : DiabotCommand() {

    init {
        this.name = "reply"
        this.help = "replies in a bunch of weird ways"
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
        this.category = category
    }

    override fun execute(event: CommandEvent) {
        event.reply("normal reply")
        event.replySuccess("success")
        event.replyWarning("warning")
        event.replyError("error")

        val builder = EmbedBuilder()

        builder.setTitle("cool title")
        builder.setAuthor("Cas Eliëns", "https://dongtronic.com")
        builder.addField("field 1", "the first field value, inline `false`", false)
        builder.addField("Field 2", "the second field value, inline `true`", true)

        builder.setImage("https://i.eliens.co/1539536815557.jpg")
        builder.setThumbnail("https://i.eliens.co/1539536815557.jpg")

        builder.setDescription("the description")
        builder.appendDescription("\nSome more description")
        builder.appendDescription("\nLorem ipsum dolor sit amet, consectetur adipiscing elit.\n")

        builder.setFooter("this is the footer", "https://i.eliens.co/1539537165227.gif")

        builder.setColor(Color.magenta)

        val embed = builder.build()

        event.reply(embed)
    }
}
