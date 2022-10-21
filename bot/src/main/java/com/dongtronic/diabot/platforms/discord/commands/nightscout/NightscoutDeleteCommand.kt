package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.logic.NightscoutFacade
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult

class NightscoutDeleteCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

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
        val allData = event.args.contains("all", ignoreCase = true)

        val result = if (allData) {
            NightscoutFacade.clearAll(event.author)
        } else {
            NightscoutFacade.clearUrl(event.author)
        }

        result.subscribe({
            if (it is UpdateResult) {
                event.reply("Removed Nightscout URL for ${event.author.name}")
            } else if (it is DeleteResult) {
                event.reply("Removed all Nightscout data for ${event.author.name}")
            }
        }, {
            logger.warn("Could not delete NS data", it)
            event.replyError("An error occurred while deleting data for ${event.author.name}")
        })
    }
}
