package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import reactor.core.publisher.Mono

class NightscoutPublicCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {
    private val nightscoutDAO = NightscoutDAO.instance
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
        val result: Mono<Boolean> = if(args.isEmpty()) {
            // toggle visibility if no arguments are provided
            nightscoutDAO.changePrivacy(event.author.idLong, event.guild.idLong, null)
        } else {
            val mode = args[0].toUpperCase()

            if (mode == "TRUE" || mode == "T" || mode == "YES" || mode == "Y" || mode == "ON") {
                nightscoutDAO.changePrivacy(event.author.idLong, event.guild.idLong, true)
            } else {
                nightscoutDAO.changePrivacy(event.author.idLong, event.guild.idLong, false)
            }
        }

        reply(event, result)
    }

    fun reply(event: CommandEvent, result: Mono<Boolean>) {
        val authorNick = NicknameUtils.determineAuthorDisplayName(event)
        result.subscribe({ public ->
            val visibility = if (public) "public" else "private"
            event.reply("Nightscout data for $authorNick set to **$visibility** in **${event.guild.name}**")
        }, {
            event.replyError("Nightscout data for $authorNick could not be changed in **${event.guild.name}**")
            logger.warn("Could not change Nightscout privacy", it)
        })
    }
}
