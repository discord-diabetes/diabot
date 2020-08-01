package com.dongtronic.diabot.platforms.discord.commands.admin.channels

import com.dongtronic.diabot.data.redis.AdminDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class AdminChannelDeleteCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {
    private val logger by Logger()

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

            if (!StringUtils.isNumeric(args[0])) {
                throw IllegalArgumentException("Channel ID must be numeric")
            }

            val channelId = args[0]

            val channel = event.jda.getTextChannelById(channelId)
                    ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

            AdminDAO.getInstance().removeAdminChannel(event.guild.id, channelId)

            event.reply("Removed admin channel ${channel.name} (`$channelId`)")
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }
}