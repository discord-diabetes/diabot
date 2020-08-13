package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User

class NightscoutDeleteCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "delete"
        this.help = "Delete Nightscout URL"
        this.guildOnly = false
        this.ownerCommand = false
        this.aliases = arrayOf("d", "del", "r", "rm", "remove")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " delete")
    }

    override fun execute(event: CommandEvent) {
        removeNightscoutUrl(event.author)
        event.reply("Removed Nightscout URL for ${event.author.name}")
    }

    private fun removeNightscoutUrl(user: User) {
        NightscoutDAO.getInstance().removeNIghtscoutUrl(user)
    }
}
