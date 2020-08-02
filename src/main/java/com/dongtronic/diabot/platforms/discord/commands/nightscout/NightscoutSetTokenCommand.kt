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
        setNightscoutToken(event.author, token).subscribe({
            if (token != null) {
                event.replySuccess("Set Nightscout token for ${event.author.name}")
            } else {
                event.replySuccess("Deleted Nightscout token for ${event.author.name}")
            }
        }, {
            logger.warn("Could not set Nightscout token", it)
            event.replyError("An error occurred while setting Nightscout token for ${event.author.name}")
        })
    }

    private fun delete(event: CommandEvent) {
        try {
            event.message.delete().reason("privacy").queue()
        } catch (ex: InsufficientPermissionException) {
            logger.info("Could not remove command message due to missing permission: ${ex.permission}")
            event.replyError("Could not remove command message due to missing `${ex.permission}` permission. Please remove the message yourself to protect your privacy.")
        }
    }

    private fun setNightscoutToken(user: User, token: String?): Mono<UpdateResult> {
        return NightscoutDAO.instance.setToken(user.idLong, token)
    }
}
