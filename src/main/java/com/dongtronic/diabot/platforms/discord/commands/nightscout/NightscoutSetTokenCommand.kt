package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User
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

        try {
            when {
                args.size == 1 -> {
                    // set token
                    setNightscoutToken(event.author, args[0])
                    event.reply("Set Nightscout Token for ${event.author.name}")
                    event.message.delete().reason("privacy").queue()
                }
                args.isEmpty() -> {
                    // delete token
                    deleteNightscoutToken(event.author)
                    event.replySuccess("Deleted Nightscout Token for ${event.author.name}")
                }
                else -> event.replyError("Invalid number of arguments")
            }
        } catch (ex: InsufficientPermissionException) {
            logger.info("Could not remove command message due to missing permission: ${ex.permission}")
            event.replyError("Could not remove command message due to missing `${ex.permission}` permission. Please remove the message yourself to protect your privacy.")
        }

    }

    private fun setNightscoutToken(user: User, token: String) {
        NightscoutDAO.getInstance().setNightscoutToken(user, token)
    }

    private fun deleteNightscoutToken(user: User) {
        NightscoutDAO.getInstance().removeNightscoutToken(user)
    }
}
