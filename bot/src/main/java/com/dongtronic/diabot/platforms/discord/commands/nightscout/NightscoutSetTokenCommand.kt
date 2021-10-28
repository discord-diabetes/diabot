package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException

class NightscoutSetTokenCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "token"
        this.help = "Set Nightscout authentication token"
        this.ownerCommand = false
        this.aliases = arrayOf("t")
        this.category = category
        this.guildOnly = false
        this.examples = arrayOf(this.parent!!.name + " token <token>", this.parent.name + " token (to remove)")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size > 1) {
            event.replyError("Invalid number of arguments")
            return
        } else if (args.isNotEmpty()) {
            // delete if there's a token
            delete(event)
        }

        val token = args.getOrNull(0)

        if (token == null) {
            NightscoutFacade.clearToken(event.author).subscribe({
                event.replySuccess("Deleted Nightscout token for ${event.author.name}")
            }, {
                logger.warn("Could not clear nightscout token", it)
                event.replyError("An error occurred while clearing Nightscout token for ${event.author.name}")
            })
        } else {
            NightscoutFacade.setToken(event.author, token).subscribe({
                event.replySuccess("Set Nightscout token for ${event.author.name}")
            }, {
                logger.warn("Could not set nightscout token", it)
                event.replyError("An error occurred while setting Nightscout token for ${event.author.name}")
            })
        }
    }

    private fun delete(event: CommandEvent) {
        try {
            event.message.delete().reason("privacy").queue()
        } catch (ex: InsufficientPermissionException) {
            logger.info("Could not remove command message due to missing permission: ${ex.permission}")
            event.replyError("Could not remove command message due to missing `${ex.permission}` permission. Please remove the message yourself to protect your privacy.")
        } catch (ex: IllegalStateException) {
            logger.info("Could not delete command message. probably in a DM")
        }
    }
}
