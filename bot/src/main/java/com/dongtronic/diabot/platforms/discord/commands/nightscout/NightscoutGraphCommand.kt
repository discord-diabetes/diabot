package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.graph.BgGraph
import com.dongtronic.diabot.graph.GraphSettings
import com.dongtronic.diabot.graph.PlottingStyle
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.dongtronic.nightscout.Nightscout
import com.dongtronic.nightscout.data.NightscoutDTO
import com.jagrosh.jdautilities.command.CommandEvent
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.XYChart
import reactor.core.publisher.Mono


class NightscoutGraphCommand(category: Category) : DiscordCommand(category, null) {

    private val logger = logger()

    init {
        this.name = "nightscoutgraph"
        this.help = "Generates a graph of your recent BG data from Nightscout"
        this.guildOnly = false
        this.aliases = arrayOf("nsg", "bgg", "bsg", "nsgraph", "bggraph", "bsgraph", "graph")
        this.category = category
        this.examples = arrayOf("diabot nightscoutgraph")
    }

    override fun execute(event: CommandEvent) {
        val chart = BgGraph(GraphSettings(PlottingStyle.SCATTER))

        getDataSet(event.author.id, chart).subscribe({
            event.channel.sendFile(BitmapEncoder.getBitmapBytes(it, BitmapEncoder.BitmapFormat.PNG), "graph.png").complete()
            logger.info("Wrote")
        }, {
            logger.error("Error", it)
        })
    }

    fun getDataSet(sender: String, chart: BgGraph): Mono<XYChart> {
        return NightscoutDAO.instance.getUser(sender)
                .flatMap { userDTO ->
                    if (userDTO.url == null) {
                        return@flatMap Mono.error<NightscoutDTO>(Exception("no url found"))
                    }
                    val ns = Nightscout(userDTO.url, userDTO.token)
                    ns.getRecentSgv(count = 12*4).flatMap { ns.getSettings(it) }
                }.map {
                    chart.apply { addEntries(it) }
                }
    }
}
