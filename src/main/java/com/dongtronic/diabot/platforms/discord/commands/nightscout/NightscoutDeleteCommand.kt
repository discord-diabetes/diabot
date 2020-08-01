package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.data.mongodb.NightscoutUserDTO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import net.dv8tion.jda.api.entities.User
import reactor.core.publisher.Mono
import kotlin.reflect.KProperty

class NightscoutDeleteCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger by Logger()

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

        removeNightscoutUrl(event.author, allData).subscribe({
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

    private fun removeNightscoutUrl(user: User, allData: Boolean): Mono<*> {
        val data = if (allData) emptyArray<KProperty<*>>() else arrayOf(NightscoutUserDTO::url)
        return NightscoutDAO.instance.deleteUser(user.idLong, *data)
    }
}
