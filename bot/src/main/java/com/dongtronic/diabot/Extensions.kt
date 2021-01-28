package com.dongtronic.diabot

import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.RestAction
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

val CommandEvent.authorName: String
    get() = NicknameUtils.determineAuthorDisplayName(this)

fun CommandEvent.nameOf(user: User): String {
    return NicknameUtils.determineDisplayName(this, user)
}

fun MessageReceivedEvent.nameOf(user: User): String {
    return NicknameUtils.determineDisplayName(this, user)
}

fun <T> RestAction<T>.submitMono(): Mono<T> {
    return submit().toMono()
}

@Suppress("ReactiveStreamsUnusedPublisher")
fun <T, U : Any> Flux<T>.mapNotNull(transform: (T) -> U?): Flux<U> {
    return this.flatMap {
        val result = transform.invoke(it)
        return@flatMap result?.toMono() ?: Flux.empty<U>()
    }
}

@Suppress("ReactiveStreamsUnusedPublisher")
fun <T, U : Any> Flux<T>.flatMapNotNull(transform: (T) -> Publisher<U>?): Flux<U> {
    return this.flatMap {
        val result = transform.invoke(it)
        return@flatMap result?.toMono() ?: Flux.empty<U>()
    }
}