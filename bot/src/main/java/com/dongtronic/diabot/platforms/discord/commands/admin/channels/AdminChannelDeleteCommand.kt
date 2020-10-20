package com.dongtronic.diabot.platforms.discord.commands.admin.channels

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class AdminChannelDeleteCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {
    private val logger = logger()

    init {
        this.name = "delete"
        this.help = "Removes a channel as an admin channel"
        this.guildOnly = false
        this.aliases = arrayOf("remove", "d", "r")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        try {
            if (args.size != 1) {
                throw IllegalArgumentException("Channel ID is required")
            }

            val channel = if (event.message.mentionedChannels.size == 0) {
                if (!StringUtils.isNumeric(args[0])) {
                    throw IllegalArgumentException("Channel ID must be numeric")
                }

                val channelId = args[0]
                event.jda.getTextChannelById(channelId)
                        ?: throw IllegalArgumentException("Channel `$channelId` does not exist")
            } else {
                event.message.mentionedChannels[0]
            }

            ChannelDAO.instance.changeAttribute(event.guild.id, channel.id, ChannelDTO.ChannelAttribute.ADMIN, false)
                    .subscribe({
                        event.replySuccess("Removed admin channel ${channel.name} (`${channel.id}`)")
                    }, {
                        val msg = "Could not remove admin channel ${channel.name} (${channel.id})"
                        logger.warn(msg, it)
                        event.replyError(msg)
                    })
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }
}