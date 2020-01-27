package com.dongtronic.diabot.platforms.discord.utils

import com.dongtronic.diabot.data.AdminDAO
import com.dongtronic.diabot.exceptions.NotAnAdminChannelException
import com.jagrosh.jdautilities.command.CommandEvent

object CommandUtils {

    fun requireAdminChannel(event: CommandEvent): Boolean {
        return try {
            val adminChannels = AdminDAO.getInstance().listAdminChannels(event.guild.id)
                    ?: throw NotAnAdminChannelException()

            if (!adminChannels.contains(event.channel.id)) {
                throw NotAnAdminChannelException()
            }

            true
        } catch (ex: NotAnAdminChannelException) {
            event.replyError("This command can only be executed in an admin channel")
            false
        }
    }
}
