package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.graph.PlottingStyle
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class NightscoutGraphModeCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {
    private val logger = logger()

    init {
        this.name = "mode"
        this.help = "Switches the plotting style of the graph between a scatter plot and a line plot"
        this.guildOnly = true
        this.aliases = arrayOf("plot", "m")
        this.examples = arrayOf(this.parent!!.name + " mode [scatter/line]")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val mode = NightscoutDAO.instance.getUser(event.author.id)
                .map { it.graphSettings }
                .map { settings ->
                    val newStyle = if (args.isEmpty() || args[0].isBlank()) {
                        PlottingStyle.values().first { it != settings.plotMode }
                    } else {
                        PlottingStyle.values().first { it.name.startsWith(args[0], true) }
                    }

                    settings.copy(plotMode = newStyle)
                }

        val command = mode.flatMap {
            NightscoutDAO.instance.updateGraphSettings(event.author.id, it)
        }

        command.subscribe({
            event.replySuccess("Plotting style changed to `${it.plotMode.name}`")
        }, {
            if (it is NoSuchElementException) {
                event.replyError("No plotting mode known by the name of `${args[0]}`")
            } else {
                event.replyError("Could not update plotting style")
                logger.warn("Unexpected error when changing graph mode for ${event.author}", it)
            }
        })
    }
}