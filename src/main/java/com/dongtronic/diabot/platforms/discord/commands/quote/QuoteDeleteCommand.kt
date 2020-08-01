package com.dongtronic.diabot.platforms.discord.commands.quote

import com.dongtronic.diabot.data.mongodb.QuoteDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mongodb.client.result.DeleteResult
import net.dv8tion.jda.api.Permission
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

class QuoteDeleteCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val logger by Logger()

    init {
        this.name = "delete"
        this.help = "Deletes a quote by its ID"
        this.guildOnly = true
        this.aliases = arrayOf("remove", "del", "d", "rm")
        this.examples = arrayOf(this.parent!!.name + " delete 1337")
    }

    override fun execute(event: CommandEvent) {
        if (!QuoteDAO.checkRestrictions(event.textChannel, warnDisabledGuild = true, checkQuoteLimit = false)) return

        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (args.isEmpty() || args[0].isBlank()) {
            event.replyError("No quote ID specified")
            return
        }
        val quoteId = args[0].toLongOrNull()
        if (quoteId == null) {
            event.replyError("Quote ID must be numeric")
            return
        }

        val deleteCommand = QuoteDAO.getInstance().deleteQuote(event.guild.idLong, quoteId)
        var execution: Mono<DeleteResult> = deleteCommand

        if (!event.member.hasPermission(Permission.MESSAGE_MANAGE)) {
            execution = QuoteDAO.getInstance().getQuote(event.guild.idLong, quoteId).flatMap {
                if (it.authorId == event.author.idLong) {
                    // this will be mapped to the delete command
                    it.toMono()
                } else {
                    event.replyError("You may not delete this quote as you are not the author of it.")
                    // this will end the execution
                    Mono.empty()
                }
            }.flatMap { deleteCommand }
        }

        execution.subscribe({
            if (it.wasAcknowledged() && it.deletedCount != 0L) {
                event.replySuccess("Quote #$quoteId deleted")
            } else {
                event.replyError("No quote found for #$quoteId")
            }
        }, {
            if (it is NoSuchElementException) {
                event.replyError("No quote found for #$quoteId")
            } else {
                event.replyError("Could not delete quote #$quoteId")
                logger.warn("Unexpected error: " + it::class.simpleName + " - " + it.message)
            }
        })
    }
}