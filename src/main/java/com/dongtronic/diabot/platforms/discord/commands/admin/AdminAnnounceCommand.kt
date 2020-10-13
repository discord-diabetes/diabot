package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class AdminAnnounceCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "announce"
        this.help = "Announce a message in a channel"
        this.arguments = "<channel ID or #mention> <message>"
        this.guildOnly = true
        this.ownerCommand = false
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size < 2) {
            event.replyError("Please supply at least 2 arguments (channel ID and message)")
            return
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

        val message = event.args.substringAfter(' ')

        channel.sendMessage(message).queue()

        event.reply("Sent announcement to ${channel.asMention} (`${channel.id}`)")
    }
}
