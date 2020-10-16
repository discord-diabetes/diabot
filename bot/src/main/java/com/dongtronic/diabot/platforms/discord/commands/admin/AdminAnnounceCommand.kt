package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminAnnounceCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "announce"
        this.help = "Announce a message in a channel"
        this.arguments = "<channel ID> <message>"
        this.guildOnly = true
        this.ownerCommand = false
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size < 2) {
            event.replyError("Please supply at least 2 arguments (channel ID and message)")
            return
        }

        val channelId = args[0]
        val channel = event.guild.getTextChannelById(channelId)

        if (channel == null) {
            event.replyError("Channel `$channelId` does not exist")
            return
        }

        val message = event.args.substring(channelId.length)

        channel.sendMessage(message).queue()

        event.reply("Sent announcement to ${channel.asMention} (`$channelId`)")
    }
}
