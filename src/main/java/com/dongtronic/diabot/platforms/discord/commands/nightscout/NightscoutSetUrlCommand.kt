package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mongodb.client.result.UpdateResult
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import reactor.core.publisher.Mono

class NightscoutSetUrlCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    init {
        this.name = "set"
        this.help = "Set Nightscout URL"
        this.ownerCommand = false
        this.aliases = arrayOf("s")
        this.category = category
        this.guildOnly = false
        this.examples = arrayOf(this.parent!!.name + " set <url>")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if(args.isEmpty()) {
            event.replyError("nightscout URL is required")
            return
        }

        setNightscoutUrl(event.author, args[0]).subscribe({
            event.reply("Set Nightscout URL for ${event.author.name}")
        }, {
            logger.warn("Could not set NS URL", it)
            if (it is IllegalArgumentException)
                event.replyError(it.message)
            else
                event.replyError("Could not set URL")
        })

        try {
            event.message.delete().reason("privacy").queue()
        } catch (ex: InsufficientPermissionException) {
            logger.info("Could not remove command message due to missing permission: ${ex.permission}")
            event.replyError("Could not remove command message due to missing `${ex.permission}` permission. Please remove the message yourself to protect your privacy.")
        }
    }

    private fun setNightscoutUrl(user: User, url: String): Mono<UpdateResult> {
        val finalUrl = validateNightscoutUrl(url)
        return NightscoutDAO.instance.setUrl(user.idLong, finalUrl)
    }

    companion object {
        private val logger = logger()

        fun validateNightscoutUrl(url: String): String {
            var finalUrl = url
            if (!finalUrl.contains("http://") && !finalUrl.contains("https://")) {
                logger.warn("Missing scheme in Nightscout URL: $finalUrl, adding https://")
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
}
