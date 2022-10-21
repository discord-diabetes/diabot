package com.dongtronic.diabot.platforms.discord.utils

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.jagrosh.jdautilities.command.CommandEvent
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

object CommandUtils {

    fun requireAdminChannel(event: CommandEvent): Mono<Boolean> {
        return ChannelDAO.instance.hasAttribute(event.channel.id, ChannelDTO.ChannelAttribute.ADMIN)
                // assume it's not an admin channel if an error occurred
                .onErrorReturn(false)
                .flatMap {
                    return@flatMap if (!it) {
                        event.replyError("This command can only be executed in an admin channel")
                        Mono.empty()
                    } else {
                        it.toMono()
                    }
                }
    }
}
