package com.dongtronic.diabot.platforms.discord.commands.admin.channels

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
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
        ChannelDAO.instance.getChannels(event.guild.id)
            .filter { it.attributes.contains(ChannelDTO.ChannelAttribute.ADMIN) }
            .mapNotNull { event.guild.getTextChannelById(it.channelId) }
            .collectList()
            .subscribe({ channels ->
                val builder = EmbedBuilder()

                builder.setTitle("Admin channels")
                if (channels.isEmpty()) {
                    builder.setDescription("No admin channels are configured")
                } else {
                    channels.forEach {
                        builder.appendDescription("**${it!!.name}**  (`${it.id}`)\n")
                    }
                }
                event.reply(builder.build())
            }, {
                val msg = "Could not access list of admin channels"
                logger.warn(msg + " for ${event.guild.id}", it)
                event.replyError(msg)
            })
    }
}
