package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.ChannelDAO
import com.dongtronic.diabot.data.mongodb.ChannelDTO
import com.dongtronic.diabot.mapNotNull
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.CommandUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class NightscoutAdminSimpleListCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "list"
        this.help = "List all channels where diabot will use simple nightscout replies"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("l")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " list")
        this.userPermissions = this.parent.userPermissions
    }

    override fun execute(event: CommandEvent) {
        CommandUtils.requireAdminChannel(event).subscribe { runCommand(event) }
    }

    private fun runCommand(event: CommandEvent) {
        ChannelDAO.instance.getChannels(event.guild.id)
                .filter { it.attributes.contains(ChannelDTO.ChannelAttribute.NIGHTSCOUT_SHORT) }
                .mapNotNull { event.guild.getTextChannelById(it.channelId) }
                .collectList()
                .subscribe({ channels ->
                    val builder = EmbedBuilder()

                    builder.setTitle("Short Nightscout channels")

                    if (channels.isEmpty()) {
                        builder.setDescription("No short channels are configured")
                    } else {
                        channels.forEach {
                            builder.appendDescription("**${it.name}**  (`${it.id}`)\n")
                        }
                    }

                    event.reply(builder.build())
                }, {
                    val msg = "Could not access list of short Nightscout channels"
                    logger.warn(msg + " for ${event.guild.id}", it)
                    event.replyError(msg)
                })
    }
}
