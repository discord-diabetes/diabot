package com.dongtronic.diabot.commands

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.exceptions.NotAnAdminChannelException
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class NightscoutAdminCommand(category: Command.Category) : DiabotCommand() {

    private val logger = LoggerFactory.getLogger(NightscoutAdminCommand::class.java)

    init {
        this.name = "nightscoutadmin"
        this.help = "Administrator commands for nightscout"
        this.guildOnly = true
        this.aliases = arrayOf("nsadmin", "na")
        this.category = category
        this.examples = arrayOf("diabot nsadmin list", "diabot nsadmin set <userId> <url>", "diabot nsadmin delete <userId>")
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            throw IllegalArgumentException("must include operation")
        }

        val command = args[0].toUpperCase()

        try {
            when (command) {
                "LIST", "L", "SHOW" -> listUsers(event)
                "DELETE", "REMOVE", "D", "R" -> deleteNightscoutUrl(event)
                "SET", "S" -> setNightscoutUrl(event)
                else -> {
                    throw IllegalArgumentException("unknown command $command")
                }
            }
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }

    }

    private fun listUsers(event: CommandEvent) {
        if(!CommandUtils.requireAdminChannel(event)) {
            return
        }

        logger.info("Listing all nightscout URLs for ${event.author.name}")
        val users = NightscoutDAO.getInstance().listUsers()

        val builder = EmbedBuilder()

        builder.setTitle("Nightscout users")

        for ((user, value) in users) {
            val userId = user.substring(0, user.indexOf(":"))

            val username = event.jda.getUserById(userId).name


            builder.appendDescription("**$username** ($userId) -> $value\n")
        }

        event.reply(builder.build())
    }

    private fun deleteNightscoutUrl(event: CommandEvent) {
        try {
            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (args.size != 2) {
                logger.debug("args length: ${args.size}")
                throw IllegalArgumentException("User ID is required as argument")
            }

            if (!StringUtils.isNumeric(args[1])) {
                throw IllegalArgumentException("User ID must be numeric")
            }

            val userId = args[1]

            val user = event.jda.getUserById(userId) ?: throw IllegalArgumentException("User with ID `$userId` does not exist in this server")

            logger.info("Deleting nightscout URL for user $userId [requested by ${event.author.name}]")

            val existingUrl = NightscoutDAO.getInstance().getNightscoutUrl(user)

            if(existingUrl.isNullOrBlank()) {
                event.reply("User **${user.name}** (`$userId`) does not have a nightscout URL configured")
                return
            }

            NightscoutDAO.getInstance().removeNIghtscoutUrl(user)

            event.replySuccess("Deleted nightscout URL for user **${user.name}** (`$userId`)")

        } catch (ex: NullPointerException) {
            throw IllegalArgumentException("Invalid user ID provided")
        }
    }

    private fun setNightscoutUrl(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if(args.size != 3) {
            throw IllegalArgumentException("Please provide the <userId> and <url> parameters")
        }

        if(!StringUtils.isNumeric(args[1])) {
            throw IllegalArgumentException("User ID must be numeric")
        }

        val url = validateNightscoutUrl(args[2])

        val user = event.jda.getUserById(args[1]) ?: throw IllegalArgumentException("User `${args[1]}` does not exist in this server")

        logger.info("Admin setting URL for user ${args[1]} to ${args[2]}")

        NightscoutDAO.getInstance().setNightscoutUrl(user, url)

        event.reply("Admin set nightscout URL for ${user.name} [requested by ${event.author.name}]")
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
}
