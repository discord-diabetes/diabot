package com.dongtronic.diabot.platforms.discord.utils

import com.dongtronic.diabot.submitMono
import com.jagrosh.jdautilities.command.CommandEvent
import dev.minn.jda.ktx.coroutines.await
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

object NicknameUtils {

    fun determineAuthorDisplayName(event: CommandEvent): Mono<String> {
        return determineDisplayName(event, event.author)
    }

    fun determineDisplayName(event: CommandEvent, user: User): Mono<String> {
        val fallback = user.name.toMono()

        return if (event.channelType == ChannelType.TEXT) {
            event.guild.retrieveMember(user).submitMono()
                .map { it.effectiveName }
                .onErrorResume { fallback }
        } else {
            fallback
        }
    }

    suspend fun suspendDetermineDisplayName(event: CommandEvent, user: User): String {
        val fallback = user.name

        return try {
            val member = event.guild.retrieveMember(user).await()
            member.effectiveName
        } catch (_: Exception) {
            fallback
        }
    }
}
