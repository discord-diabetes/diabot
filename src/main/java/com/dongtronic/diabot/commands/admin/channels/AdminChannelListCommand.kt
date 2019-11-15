package com.dongtronic.diabot.commands.admin.channels

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.LoggerFactory

class AdminChannelListCommand(category: Category, parent: Command?) : DiabotCommand(category, parent) {
    private val logger = LoggerFactory.getLogger(AdminChannelListCommand::class.java)

    init {
        this.name = "list"
        this.help = "Lists admin channels"
        this.guildOnly = false
        this.aliases = arrayOf("l")
    }

    override fun execute(event: CommandEvent) {
        val channels = AdminDAO.getInstance().listAdminChannels(event.guild.id)

        val builder = EmbedBuilder()

        builder.setTitle("Admin channels")

        for (channelId in channels!!) {
            val channel = event.jda.getTextChannelById(channelId)

            builder.appendDescription("**${channel!!.name}** (`${channel.id}`)\n")
        }

        event.reply(builder.build())
    }
}