package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class AdminChannelsCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminChannelsCommand::class.java)

    init {
        this.name = "channels"
        this.help = "Admin channel settings"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("c")
        this.examples = arrayOf("diabot admin channels add <channelId>", "diabot admin channels delete <channelId>", "diabot admin channels list")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            event.replyError("must include operation")
            return
        }

        val command = args[0].toUpperCase()

        try {
            when (command) {
                "LIST", "L" -> listChannels(event)
                "ADD", "A" -> addChannel(event)
                "DELETE", "REMOVE", "D", "R" -> deleteChannel(event)
                else -> {
                    throw IllegalArgumentException("unknown command $command")
                }
            }

        } catch (ex: IllegalArgumentException) {
            event.replyError(ex.message)
        }
    }

    private fun listChannels(event: CommandEvent) {
        val channels = AdminDAO.getInstance().listAdminChannels(event.guild.id)

        val builder = EmbedBuilder()

        builder.setTitle("Admin channels")

        for (channelId in channels!!) {
            val channel = event.jda.getTextChannelById(channelId)

            builder.appendDescription("**${channel!!.name}** (`${channel.id}`)\n")
        }

        event.reply(builder.build())
    }

    private fun addChannel(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 2) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[1])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[1]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().addAdminChannel(event.guild.id, channelId)

        event.reply("Added admin channel ${channel.name} (`$channelId`)")
    }

    private fun deleteChannel(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 2) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[1])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[1]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().removeAdminChannel(event.guild.id, channelId)

        event.reply("Removed admin channel ${channel.name} (`$channelId`)")

    }
}
