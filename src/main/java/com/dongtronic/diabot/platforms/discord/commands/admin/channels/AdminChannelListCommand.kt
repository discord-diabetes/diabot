package com.dongtronic.diabot.platforms.discord.commands.admin.channels

import com.dongtronic.diabot.data.AdminDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class AdminChannelListCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {
    private val logger = logger()

    init {
        this.name = "list"
        this.help = "Lists admin channels"
        this.guildOnly = false
        this.aliases = arrayOf("l")
        this.userPermissions = this.parent!!.userPermissions
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