package com.dongtronic.diabot.commands.admin.channels

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class AdminChannelDeleteCommand(category: Category, parent: Command?) : DiabotCommand(category, parent) {
    private val logger = LoggerFactory.getLogger(AdminChannelDeleteCommand::class.java)

    init {
        this.name = "delete"
        this.help = "Removes a channel as an admin channel"
        this.guildOnly = false
        this.aliases = arrayOf("remove", "d", "r")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 1) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[0])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[0]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().removeAdminChannel(event.guild.id, channelId)

        event.reply("Removed admin channel ${channel.name} (`$channelId`)")
    }
}