package com.dongtronic.diabot.util

import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.entities.User

object NicknameUtils {

    fun determineAuthorDisplayName(event: CommandEvent): String {
        return determineDisplayName(event, event.author)
    }

    fun determineDisplayName(event: CommandEvent, user: User): String {
        return event.guild.getMember(user).effectiveName
    }
}
