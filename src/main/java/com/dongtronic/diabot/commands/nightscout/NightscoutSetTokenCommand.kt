package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.exceptions.InsufficientPermissionException
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class NightscoutSetTokenCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutSetTokenCommand::class.java)

    init {
        this.name = "token"
        this.help = "Set Nightscout authentication token"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("t")
        this.category = category
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
            event.replyError("Could not remove command message due to missing `manage messages` permission. Please remove the message yourself to protect your privacy.")
        }

    }

    private fun setNightscoutToken(user: User, token: String) {
        NightscoutDAO.getInstance().setNightscoutToken(user, token)
    }

    private fun deleteNightscoutToken(user: User) {
        NightscoutDAO.getInstance().setNightscoutToken(user, "")
    }
}
