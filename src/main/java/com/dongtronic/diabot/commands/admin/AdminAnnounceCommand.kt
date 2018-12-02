package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminAnnounceCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminAnnounceCommand::class.java)

    init {
        this.name = "announce"
        this.help = "Announce a message in a channel"
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
