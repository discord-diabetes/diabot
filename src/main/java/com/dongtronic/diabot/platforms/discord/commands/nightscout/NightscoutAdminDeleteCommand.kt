package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class NightscoutAdminDeleteCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "delete"
        this.help = "Delete a configured nightscout URL"
        this.guildOnly = true
        this.ownerCommand = true
        this.aliases = arrayOf("d", "del", "r", "rm", "remove")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " delete <userId>")
    }

    override fun execute(event: CommandEvent) {

        // This command may exclusively be run on the official Diabetes server.
        if (event.guild.id != System.getenv("HOME_GUILD_ID")) {
            event.replyError(System.getenv("HOME_GUILD_MESSAGE"))
            return
        }

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
                event.reply("User **${NicknameUtils.determineDisplayName(event, user)}** (`$userId`) does not have a Nightscout URL configured")
                return
            }

            NightscoutDAO.getInstance().removeNIghtscoutUrl(user)

            event.replySuccess("Deleted Nightscout URL for user **${NicknameUtils.determineDisplayName(event, user)}** (`$userId`)")

        } catch (ex: NullPointerException) {
            event.replyError("Invalid user ID provided")
        }
    }
}
