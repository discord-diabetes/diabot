package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class QuoteEditCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val quoteRegex = Regex("(?<qid>.*) \"(?<message>[\\s\\S]*)\" ?- ?(?<author>.*[^\\s])")
    private val logger = logger()

    init {
        this.name = "edit"
        this.help = "Edits an existing quote by its ID"
        this.guildOnly = true
        this.aliases = arrayOf("e")
        this.userPermissions = arrayOf(Permission.MESSAGE_MANAGE)
        this.examples = arrayOf(this.parent!!.name + " edit 1337 \"edited quote\" - edited author")
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        val match = quoteRegex.matchEntire(event.args)
        if (match == null) {
            event.replyError("Could not parse command. Please make sure you are using the correct format for this command")
            return
        }

        val id = match.groups["qid"]!!.value.trim().toLongOrNull()
        if (id == null) {
            event.replyError("Quote ID must be numeric")
            return
        }

        val author = match.groups["author"]!!.value.trim()
        val message = match.groups["message"]!!.value.trim()
        val oldQuote = QuoteDAO.getInstance().getQuote(event.guild.idLong, id)

        oldQuote.flatMap {
            val dto = it.copy(
                    author = author,
                    message = message,
                    messageId = event.message.idLong,
                    channelId = event.channel.idLong)
            QuoteDAO.getInstance().updateQuote(dto)
        }.subscribe({
            if (it.wasAcknowledged()) {
                event.replySuccess("Quote #$id edited")
            } else {
                event.replyError("Could not edit quote #$id")
            }
        }, {
            if (it is NoSuchElementException) {
                event.replyError("No quote found for #$id")
            } else {
                event.replyError("Could not edit quote #$id")
                logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
            }
        })

    }
}