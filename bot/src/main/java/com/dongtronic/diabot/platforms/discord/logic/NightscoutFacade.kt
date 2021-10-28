package com.dongtronic.diabot.platforms.discord.logic

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.util.logger
import com.mongodb.client.result.UpdateResult
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import reactor.core.publisher.Mono

object NightscoutFacade {
    private val logger = logger()

    fun setToken(user: User, token: String): Mono<UpdateResult> {
        return NightscoutDAO.instance.setToken(user.id, token)
    }

    fun setUrl(user: User, url: String): Mono<UpdateResult> {
        val finalUrl = validateNightscoutUrl(url)
        return NightscoutDAO.instance.setUrl(user.id, finalUrl)
    }

    fun setPublic(user: User, guild: Guild, public: Boolean): Mono<Boolean> {
        return NightscoutDAO.instance.changePrivacy(user.id, guild.id, public)
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

    fun validateNightscoutUrl(url: String): String {
        var finalUrl = url
        if (!finalUrl.contains("http://") && !finalUrl.contains("https://")) {
            logger.info("Missing scheme in Nightscout URL: $finalUrl, adding https://")
            finalUrl = "https://$finalUrl"
        }

        if (finalUrl.endsWith("/")) {
            finalUrl = finalUrl.trimEnd('/')
        }

        if (finalUrl.endsWith("/api/v1")) {
            finalUrl = finalUrl.removeSuffix("/api/v1")
        }

        return finalUrl
    }
}
