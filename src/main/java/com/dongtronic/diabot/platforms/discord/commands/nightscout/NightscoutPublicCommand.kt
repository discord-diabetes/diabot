package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class NightscoutPublicCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "public"
        this.help = "Make your Nightscout data public or private"
        this.guildOnly = false
        this.ownerCommand = false
        this.aliases = arrayOf("pub", "p")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " public on")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if(args.isEmpty()) {
            // toggle visibility if no arguments are provided
            val newVisibility = !NightscoutDAO.getInstance().isNightscoutPublic(event.author, event.guild.id)
            NightscoutDAO.getInstance().setNightscoutPublic(event.author, event.guild, newVisibility)
            reply(event, newVisibility)
            return
        }

        val mode = args[0].toUpperCase()

        if (mode == "TRUE" || mode == "T" || mode == "YES" || mode == "Y" || mode == "ON") {
            NightscoutDAO.getInstance().setNightscoutPublic(event.author, event.guild, true)
            reply(event, true)
        } else {
            NightscoutDAO.getInstance().setNightscoutPublic(event.author, event.guild, false)
            reply(event, false)
        }
    }

    fun reply(event: CommandEvent, public: Boolean) {
        val authorNick = NicknameUtils.determineAuthorDisplayName(event)
        val visibility = if (public) "public" else "private"
        event.reply("Nightscout data for $authorNick set to **$visibility** in **${event.guild.name}**")
    }
}
