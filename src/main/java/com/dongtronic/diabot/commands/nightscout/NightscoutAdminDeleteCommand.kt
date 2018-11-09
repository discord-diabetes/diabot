package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class NightscoutAdminDeleteCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminDeleteCommand::class.java)

    init {
        this.name = "delete"
        this.help = "Delete a configured nightscout URL"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("d", "del", "r", "rm", "remove")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " delete <userId>")
    }

    override fun execute(event: CommandEvent) {
        try {
            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (args.size != 1) {
                logger.debug("args length: ${args.size}")
                throw IllegalArgumentException("User ID is required as argument")
            }

            if (!StringUtils.isNumeric(args[0])) {
                throw IllegalArgumentException("User ID must be numeric")
            }

            val userId = args[0]

            val user = event.jda.getUserById(userId) ?: throw IllegalArgumentException("User with ID `$userId` does not exist in this server")

            logger.info("Deleting Nightscout URL for user $userId [requested by ${event.author.name}]")

            val existingUrl = NightscoutDAO.getInstance().getNightscoutUrl(user)

            if(existingUrl.isNullOrBlank()) {
                event.reply("User **${user.name}** (`$userId`) does not have a Nightscout URL configured")
                return
            }

            NightscoutDAO.getInstance().removeNIghtscoutUrl(user)

            event.replySuccess("Deleted Nightscout URL for user **${user.name}** (`$userId`)")

        } catch (ex: NullPointerException) {
            event.replyError("Invalid user ID provided")
        }
    }
}
