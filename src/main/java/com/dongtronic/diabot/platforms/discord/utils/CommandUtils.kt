package com.dongtronic.diabot.platforms.discord.utils

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.exceptions.NotAnAdminChannelException
import com.jagrosh.jdautilities.command.CommandEvent

object CommandUtils {

    fun requireAdminChannel(event: CommandEvent): Boolean {
        return try {
            // todo: this needs to be converted to non-blocking
            val isAdmin = ChannelDAO.instance.hasAttribute(event.channel.idLong, ChannelDTO.ChannelAttribute.ADMIN).block()

            if (isAdmin != true) {
                throw NotAnAdminChannelException()
            }

            true
        } catch (ex: NotAnAdminChannelException) {
            event.replyError("This command can only be executed in an admin channel")
            false
        }
    }
}
