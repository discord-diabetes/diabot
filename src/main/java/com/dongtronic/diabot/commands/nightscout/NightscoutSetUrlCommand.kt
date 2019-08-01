package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException
import org.slf4j.LoggerFactory

class NightscoutSetUrlCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutSetUrlCommand::class.java)

    init {
        this.name = "set"
        this.help = "Set Nightscout URL"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("s")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " set <url>")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if(args.isEmpty()) {
            event.replyError("nightscout URL is required")
            return
        }

        try {
            setNightscoutUrl(event.author, args[0])
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }

        try {
            event.reply("Set Nightscout URL for ${event.author.name}")
            event.message.delete().reason("privacy").queue()
        } catch (ex: InsufficientPermissionException) {
            event.replyError("Could not remove command message due to missing `manage messages` permission. Please remove the message yourself to protect your privacy.")
        }
    }

    private fun validateNightscoutUrl(url: String): String {
        var finalUrl = url
        if (!finalUrl.contains("http://") && !finalUrl.contains("https://")) {
            throw IllegalArgumentException("Url must contain scheme")
        }

        if (finalUrl.endsWith("/")) {
            finalUrl = finalUrl.trimEnd('/')
        }

        return finalUrl
    }

    private fun setNightscoutUrl(user: User, url: String) {
        val finalUrl = validateNightscoutUrl(url)
        NightscoutDAO.getInstance().setNightscoutUrl(user, finalUrl)
    }
}
