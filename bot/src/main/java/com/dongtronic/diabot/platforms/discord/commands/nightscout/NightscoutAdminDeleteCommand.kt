package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.nameOf
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mongodb.client.result.UpdateResult
import org.apache.commons.lang3.StringUtils

class NightscoutAdminDeleteCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

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

            val user = if (event.message.mentions.users.size == 0) {
                if (!StringUtils.isNumeric(args[0])) {
                    throw IllegalArgumentException("User ID must be valid")
                }

                val userId = args[0]
                // this can be left unisolated because this is an owner-only command
                event.jda.getUserById(userId)
                    ?: throw IllegalArgumentException("User `$userId` is not in the server")
            } else {
                event.message.mentions.users[0]
            }

            val userId = user.id

            logger.info("Deleting Nightscout URL for user $userId [requested by ${event.author.name}]")

            NightscoutDAO.instance.deleteUser(user.id, NightscoutUserDTO::url)
                .ofType(UpdateResult::class.java)
                .subscribe({
                    if (it.modifiedCount == 0L) {
                        event.reply("User **${event.nameOf(user)}** (`$userId`) does not have a Nightscout URL configured")
                    } else {
                        event.replySuccess("Deleted Nightscout URL for user **${event.nameOf(user)}** (`$userId`)")
                    }
                }, {
                    val msg = "Could not delete Nightscout URL ${event.nameOf(user)} (`$userId`)"
                    logger.warn(msg, it)
                    event.replyError(msg)
                })
        } catch (ex: NullPointerException) {
            logger.warn("Encountered NullPointerException while attempting to delete nightscout data", ex)
            event.replyError("Invalid user ID provided")
        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }
}
