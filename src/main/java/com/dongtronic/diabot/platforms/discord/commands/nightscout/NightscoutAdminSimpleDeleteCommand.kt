package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.utils.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class NightscoutAdminSimpleDeleteCommand(category: Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminSimpleDeleteCommand::class.java)

    init {
        this.name = "delete"
        this.help = "Delete a simple response channel"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("d", "r", "remove", "del", "rem")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " delete")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        try {
            if (!CommandUtils.requireAdminChannel(event)) {
                return
            }

            val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (args.size != 1) {
                event.replyError("Please include exactly one channel ID")
                return
            }

            val channel = if (event.message.mentionedChannels.size == 0) {
                if (!StringUtils.isNumeric(args[0])) {
                    throw IllegalArgumentException("Channel ID must be numeric")
                }

                val channelId = args[0]
                event.jda.getTextChannelById(channelId)
                        ?: throw IllegalArgumentException("Channel `$channelId` does not exist")
            } else {
                event.message.mentionedChannels[0]
            }

            logger.info("Removing channel ${channel.id} as short channel for ${event.guild.id}")

            NightscoutDAO.getInstance().removeShortChannel(event.guild.id, channel.id)

            event.replySuccess("Removed channel **${channel.name}** (`${channel.id}`) as short reply channel")
        } catch (ex: Exception) {
            event.replyError(ex.message)
            return
        }
    }
}
