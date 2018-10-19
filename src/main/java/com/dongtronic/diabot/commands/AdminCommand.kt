package com.dongtronic.diabot.commands

import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory

class AdminCommand(category: Command.Category) : DiabotCommand() {

    private val logger = LoggerFactory.getLogger(AdminCommand::class.java)

    init {
        this.name = "admin"
        this.help = "Administrator commands"
        this.guildOnly = true
        this.aliases = arrayOf("a")
        this.category = category
        this.examples = arrayOf("diabot admin channels add <channelId>", "diabot admin channels delete <channelId>", "diabot admin channels list")
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            throw IllegalArgumentException("must include operation")
        }

        val category = args[0].toUpperCase()
        val command = args[1].toUpperCase()

        try {
            when (category) {
                "C", "CHANNELS" -> when (command) {
                    "LIST" -> listChannels(event)
                    "DELETE", "REMOVE" -> deleteChannel(event)
                    "ADD" -> addChannel(event)
                    else -> {
                        throw IllegalArgumentException("unknown command $category $command")
                    }
                }
                else -> {
                    throw java.lang.IllegalArgumentException("unknown command $category $command")
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

            builder.appendDescription("**${channel.name}** (`${channel.id}`)\n")
        }

        event.reply(builder.build())
    }

    private fun deleteChannel(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[2]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().removeAdminChannel(event.guild.id, channelId)

        event.reply("Removed admin channel ${channel.name} (`$channelId`)")

    }

    private fun addChannel(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.size != 3) {
            throw IllegalArgumentException("Channel ID is required")
        }

        if (!StringUtils.isNumeric(args[2])) {
            throw IllegalArgumentException("Channel ID must be numeric")
        }

        val channelId = args[2]

        val channel = event.jda.getTextChannelById(channelId)
                ?: throw IllegalArgumentException("Channel `$channelId` does not exist")

        AdminDAO.getInstance().addAdminChannel(event.guild.id, channelId)

        event.reply("Added admin channel ${channel.name} (`$channelId`)")
    }
}
