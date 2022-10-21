package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.GraphDisableDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class NightscoutAdminGraphCommand(category: Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "graph"
        this.help = "Controls if the `nightscoutgraph` command is enabled. Toggles with no argument"
        this.arguments = "[setting]"
        this.aliases = arrayOf("graphing", "graphs", "g")
        this.guildOnly = true
        this.ownerCommand = false
        this.category = category
        this.examples = arrayOf(
                this.parent!!.name + " graph",
                this.parent.name + " graph false"
        )
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val setting = kotlin.runCatching {
            args.getOrNull(0)?.toBooleanStrict()
        }.getOrElse {
            event.replyError("Could not parse argument: ${it.message}")
            return
        }

        GraphDisableDAO.instance.changeGraphEnabled(event.guild.id, setting).subscribe({
            val newStatus = if (it) "enabled" else "disabled"

            event.replySuccess("Nightscout graphing has been $newStatus")
        }, {
            event.replyError("Could not change graph enabled status: ${it.javaClass.simpleName}")
            logger.error("Could not change graph enabled status for ${event.guild.id}", it)
        })
    }
}
