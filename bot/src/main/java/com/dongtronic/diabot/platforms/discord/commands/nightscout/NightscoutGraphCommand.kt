package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.graph.BgGraph
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.dongtronic.nightscout.EntriesParameters
import com.dongtronic.nightscout.Nightscout
import com.dongtronic.nightscout.data.NightscoutDTO
import com.jagrosh.jdautilities.command.CommandEvent
import org.knowm.xchart.BitmapEncoder
import org.knowm.xchart.XYChart
import org.litote.kmongo.MongoOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.time.Duration
import java.time.Instant
import kotlin.math.min


class NightscoutGraphCommand(category: Category) : DiscordCommand(category, null) {

    private val logger = logger()

    init {
        this.name = "nightscoutgraph"
        this.help = "Generates a graph of your recent BG data from Nightscout"
        this.guildOnly = false
        this.aliases = arrayOf("nsg", "bgg", "bsg", "nsgraph", "bggraph", "bsgraph", "graph")
        this.category = category
        this.examples = arrayOf("diabot nightscoutgraph")
        this.children = arrayOf(NightscoutGraphModeCommand(category, this))
        this.cooldown = 5
        this.cooldownScope = CooldownScope.USER
    }

    override fun execute(event: CommandEvent) {
        getDataSet(event.author.id)
                .map { BitmapEncoder.getBitmapBytes(it, BitmapEncoder.BitmapFormat.PNG) }
                .flatMap { event.channel.sendFile(it, "graph.png").submit().toMono() }
                .subscribe({}, {
                    event.reactError()
                    logger.error("Error generating NS graph for ${event.author}", it)
                })
    }

    private fun getDataSet(sender: String, time: Duration = Duration.ofHours(4)): Mono<XYChart> {
        return NightscoutDAO.instance.getUser(sender)
                .zipWhen { userDTO ->
                    if (userDTO.url == null) {
                        return@zipWhen Mono.error<NightscoutDTO>(Exception("no url found"))
                    }

                    val ns = Nightscout(userDTO.url, userDTO.token)

                    // calculate the amount of readings there should be.
                    // assume 1 reading every 5 minutes and a minimum of 1 reading
                    val count = min(time.toMinutes() / 5, 1).toInt()
                    val startTime = Instant.now()
                            .minus(time)
                            .toEpochMilli()
                            .toString()
                    val findParam = EntriesParameters()
                            .find("sgv", operator = MongoOperator.exists)
                            .find("date", startTime, MongoOperator.gte)
                            .count(count)
                            .toMap()
                    ns.getSgv(params = findParam).flatMap { ns.getSettings(it) }
                }.map { tuple ->
                    val userDTO = tuple.t1
                    val ns = tuple.t2
                    BgGraph(userDTO.graphSettings).addEntries(ns)
                }
    }
}
