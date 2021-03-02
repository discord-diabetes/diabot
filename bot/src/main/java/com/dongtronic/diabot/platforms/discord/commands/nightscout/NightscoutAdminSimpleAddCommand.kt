package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.CommandUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.apache.commons.lang3.StringUtils

class NightscoutAdminSimpleAddCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "add"
        this.help = "Add a simple response channel"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("a")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " add")
        this.userPermissions = this.parent.userPermissions
    }

    override fun execute(event: CommandEvent) {
        CommandUtils.requireAdminChannel(event).subscribe { runCommand(event) }
    }

    private fun runCommand(event: CommandEvent) {
        try {
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
                event.guild.getTextChannelById(channelId)
                        ?: throw IllegalArgumentException("Channel `$channelId` does not exist")
            } else {
                event.message.mentionedChannels[0]
            }

            logger.info("Adding channel ${channel.id} as short channel for ${event.guild.id}")

            ChannelDAO.instance.changeAttribute(event.guild.id, channel.id, ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT)
                    .subscribe({
                        event.replySuccess("Set channel **${channel.name}** (`${channel.id}`) as short reply channel")
                    }, {
                        val msg = "Could not set channel ${channel.name} (${channel.id}) as short reply channel"
                        logger.warn(msg, it)
                        event.replyError(msg)
                    })
        } catch (ex: Exception) {
            event.replyError(ex.message)
            return
        }
    }
}
