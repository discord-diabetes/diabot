package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException
import java.net.URL

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

    private val logger = logger()

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            event.replyError("nightscout URL is required")
            return
        }

        NightscoutFacade.setUrl(event.author, args[0]).subscribe({
            val urlParams = getUrlParams(args[0])
            val token = urlParams["token"]
            if (token != null) {
                NightscoutFacade.setToken(event.author, token).subscribe({
                    event.replySuccess("Set Nightscout URL and token for ${event.author.name}")
                }, {
                    logger.warn("Could not set nightscout token", it)
                })
            }
            event.reply("Set Nightscout URL for ${event.author.name}")
        }, {
            logger.warn("Could not set NS URL", it)
            if (it is IllegalArgumentException) event.replyError(it.message)
            else event.replyError("Could not set URL")
        })

        try {
            event.message.delete().reason("privacy").queue()
        } catch (ex: InsufficientPermissionException) {
            logger.info("Could not remove command message due to missing permission: ${ex.permission}")
            event.replyError("Could not remove command message due to missing `${ex.permission}` permission. Please remove the message yourself to protect your privacy.")
        } catch (ex: IllegalStateException) {
            logger.info("Could not delete command message. probably in a DM")
        }
    }

    private fun getUrlParams(urlString: String): HashMap<String, String> {
        val url = URL(urlString)
        val query: String = url.query
        val params = HashMap<String, String>()
        val strParams = query.split("&")
        for (param in strParams) {
            val queryElement = param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val name = queryElement[0]
            val value = queryElement[1]
            params[name] = value
        }
        return params
    }
}
