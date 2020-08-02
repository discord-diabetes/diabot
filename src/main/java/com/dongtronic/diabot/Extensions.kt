package com.dongtronic.diabot

import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.RestAction
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val CommandEvent.authorName: String
    get() = NicknameUtils.determineAuthorDisplayName(this)

fun CommandEvent.nameOf(user: User): String {
    return NicknameUtils.determineDisplayName(this, user)
}

fun <T> RestAction<T>.submitMono(): Mono<T> {
    return submit().toMono()
}