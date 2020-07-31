package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.CommandUtils
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class NightscoutAdminSimpleListCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger by Logger()

    init {
        this.name = "list"
        this.help = "List all channels where diabot will use simple nightscout replies"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("l")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " list")
        this.userPermissions = this.parent!!.userPermissions
    }

    override fun execute(event: CommandEvent) {
        if(!CommandUtils.requireAdminChannel(event)) {
            return
        }

        val channels = NightscoutDAO.getInstance().listShortChannels(event.guild.id)

        val builder = EmbedBuilder()

        builder.setTitle("Short Nightscout channels")

        if (channels.isEmpty()) {
            builder.setDescription("No short channels are configured")
        } else {
            channels.forEach {
                val channel = event.guild.getTextChannelById(it)
                builder.appendDescription("**${channel!!.name}**  (`${channel.id}`)\n")
            }
        }

        event.reply(builder.build())
    }
}
