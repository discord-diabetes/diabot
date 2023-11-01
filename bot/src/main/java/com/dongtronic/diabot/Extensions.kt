package com.dongtronic.diabot

import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.RestAction
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Gets the command executor's display name.
 *
 * Note: this is a blocking call.
 */
val CommandEvent.authorName: String
    get() = NicknameUtils.determineAuthorDisplayName(this).block()!!

/**
 * Gets the display name of a [User].
 *
 * Note: this is a blocking call.
 *
 * @receiver CommandEvent
 * @param user The user to retrieve the display name of
 * @return the display name of the given user
 */
fun CommandEvent.nameOf(user: User): String {
    return NicknameUtils.determineDisplayName(this, user).block()!!
}

/**
 * Gets the display name of a [User].
 *
 * @receiver CommandEvent
 * @param user The user to retrieve the display name of
 * @return the display name of the given user
 */
suspend fun CommandEvent.suspendNameOf(user: User): String {
    return NicknameUtils.suspendDetermineDisplayName(this, user)
}

fun <T> RestAction<T>.submitMono(): Mono<T> {
    return submit().toMono()
}

fun <T, U : Any> Flux<T>.mapNotNull(transform: (T) -> U?): Flux<U> {
    return this.flatMap {
        val result = transform.invoke(it)
        return@flatMap result?.toMono() ?: Flux.empty()
    }
}

fun <T, U : Any> Flux<T>.flatMapNotNull(transform: (T) -> Publisher<U>?): Flux<U> {
    return this.flatMap {
        val result = transform.invoke(it)
        return@flatMap result?.toMono() ?: Flux.empty()
    }
}
