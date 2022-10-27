package com.dongtronic.diabot.platforms.discord.logic

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.util.logger
import com.mongodb.client.result.UpdateResult
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import okhttp3.HttpUrl.Companion.toHttpUrl
import reactor.core.publisher.Mono

object NightscoutFacade {
    private val logger = logger()

    fun setToken(user: User, token: String): Mono<UpdateResult> {
        return NightscoutDAO.instance.setToken(user.id, token)
    }

    fun setUrl(user: User, url: String): Mono<UpdateResult> {
        val finalUrl = parseNightscoutUrl(url)
        var update = NightscoutDAO.instance.setUrl(user.id, finalUrl.first)

        val token = finalUrl.second
        if (token != null) {
            update = update.flatMap { setToken(user, token) }
        }
        return update
    }

    fun setPublic(user: User, guild: Guild, public: Boolean): Mono<Boolean> {
        return NightscoutDAO.instance.changePrivacy(user.id, guild.id, public)
    }

    fun setGlobalPublic(user: User, public: Boolean): Mono<UpdateResult> {
        check(public) { "You can not set all guilds to public at once." }

        return NightscoutDAO.instance.changePrivacy(user.id, public)
    }

    fun clearToken(user: User): Mono<*> {
        return NightscoutDAO.instance.deleteUser(user.id, NightscoutUserDTO::token)
    }

    fun clearUrl(user: User): Mono<*> {
        return NightscoutDAO.instance.deleteUser(user.id, NightscoutUserDTO::url)
    }

    fun clearAll(user: User): Mono<*> {
        return NightscoutDAO.instance.deleteUser(user.id)
    }

    fun getUser(user: User): Mono<NightscoutUserDTO> {
        return NightscoutDAO.instance.getUser(user.id)
    }

    fun parseNightscoutUrl(url: String): Pair<String, String?> {
        var inputUrl = url
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            logger.debug("Missing scheme in Nightscout URL: $inputUrl, adding https://")
            inputUrl = "https://$inputUrl"
        }

        val parsed = inputUrl.toHttpUrl()
        val final = parsed.newBuilder()
        val token = parsed.queryParameter("token")
        // trim token
        final.removeAllQueryParameters("token")
        logger.debug("Input: $final")

        val pathSegments = parsed.pathSegments.dropLastWhile { it == "" }
        if (pathSegments.takeLast(2) == listOf("api", "v1")) {
            logger.debug("Removing API declaration in NS URL path: $pathSegments")
            val trimmedPath = pathSegments.dropLast(2).joinToString("/")
            // reset the path to nothing
            final.encodedPath("/")
            final.addPathSegments(trimmedPath)
        }

        logger.debug("Final URL: $final")

        return final.toString() to token
    }
}
