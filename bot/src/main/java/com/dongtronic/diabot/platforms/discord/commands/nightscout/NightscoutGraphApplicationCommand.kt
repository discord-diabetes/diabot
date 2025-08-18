package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.GraphDisableDAO
import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.exceptions.NightscoutFetchException
import com.dongtronic.diabot.exceptions.UnconfiguredNightscoutException
import com.dongtronic.diabot.graph.BgGraph
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.util.logger
import com.dongtronic.nightscout.EntriesParameters
import com.dongtronic.nightscout.Nightscout
import com.dongtronic.nightscout.exceptions.NoNightscoutDataException
import com.fasterxml.jackson.core.JsonProcessingException
import com.github.kaktushose.jda.commands.annotations.constraints.Max
import com.github.kaktushose.jda.commands.annotations.constraints.Min
import com.github.kaktushose.jda.commands.annotations.interactions.Command
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction
import com.github.kaktushose.jda.commands.annotations.interactions.Param
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import org.knowm.xchart.BitmapEncoder.BitmapFormat
import org.litote.kmongo.MongoOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.onErrorMap
import retrofit2.HttpException
import java.net.UnknownHostException
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.math.max

@Interaction
class NightscoutGraphApplicationCommand : ApplicationCommand {
    private val logger = logger()
    private val cooldowns = mutableMapOf<String, Long>()

    @Command("graph", desc = "Generate a graph from Nightscout")
    fun execute(
        event: CommandEvent,
        @Param("Amount of hours to display on graph", optional = true)
        @Max(24)
        @Min(1)
        hours: Long?
    ) {
        runBlocking {
            graph(event, hours)
        }
    }

    suspend fun graph(event: CommandEvent, hours: Long?) {
        val cooldownSeconds = getCooldown(event.user.id)
        if (cooldownSeconds != null) {
            val plural = if (abs(cooldownSeconds) != 1L) "s" else ""
            event.with().ephemeral(true).reply("This command is currently on a cooldown. You can use it again in $cooldownSeconds second$plural")
            return
        }

        if (hours != null && hours !in 1..24) {
            event.with().ephemeral(true).reply("The number of hours must be between 1 and 24")
            return
        }

        try {
            val enabled = !event.isFromGuild ||
                GraphDisableDAO.instance.getGraphEnabled(event.guild!!.id).awaitSingle()

            if (!enabled) {
                event.with().ephemeral(true).reply("Nightscout graphs are disabled in this guild")
                return
            }

            event.deferReply(false)

            val chart = getDataSet(event.user.id, hours).awaitSingle()
            val imageBytes = chart.getBitmapBytes(BitmapFormat.PNG)
            event.reply(MessageCreateData.fromFiles(FileUpload.fromData(imageBytes, "graph.png")))
            applyCooldown(event.user.id)
        } catch (e: Exception) {
            logger.error("Error generating NS graph for ${event.user}")
            if (e is NightscoutFetchException) {
                event.reply(NightscoutCommand.handleGrabError(e.originalException, event.user, e.userDTO))
            } else {
                event.reply(NightscoutCommand.handleError(e))
            }
        }
    }

    private fun getCooldown(id: String): Long? {
        cleanCooldowns()
        val time = cooldowns[id]

        // find time difference, then convert ms -> s
        return time?.let {
            abs(it - System.currentTimeMillis()) / 1000
        }
    }

    private fun applyCooldown(id: String, time: Duration = Duration.ofSeconds(5)) {
        cleanCooldowns()

        cooldowns[id] = System.currentTimeMillis() + time.toMillis()
    }

    private fun cleanCooldowns() {
        cooldowns.filter { it.value <= System.currentTimeMillis() }
            .forEach { cooldowns.remove(it.key) }
    }

    private fun getDataSet(senderId: String, hours: Long?): Mono<BgGraph> {
        return NightscoutDAO.instance.getUser(senderId)
            .onErrorMap(NoSuchElementException::class) { UnconfiguredNightscoutException() }
            .zipWhen { userDTO ->
                if (userDTO.url == null) {
                    return@zipWhen Mono.error(UnconfiguredNightscoutException())
                }

                val ns = Nightscout(userDTO.url, userDTO.token)

                val time = (hours ?: userDTO.graphSettings.hours)
                    .let { Duration.ofHours(it) }

                val maxReadings = max(time.toMinutes(), 1).toInt()
                val startTime = Instant.now()
                    .minus(time)
                    .toEpochMilli()
                    .toString()
                val findParam = EntriesParameters()
                    .find("sgv", operator = MongoOperator.exists)
                    .find("date", startTime, MongoOperator.gte)
                    .count(maxReadings)
                    .toMap()
                ns.getSgv(params = findParam, throwOnConversion = false)
                    // duplicate code from NightscoutCommand. will be cleaned up later with a refactor of both
                    .onErrorMap({ error ->
                        error is HttpException ||
                            error is UnknownHostException ||
                            error is JsonProcessingException ||
                            error is NoNightscoutDataException
                    }, {
                        NightscoutFetchException(userDTO, it)
                    })
                    .flatMap { ns.getSettings(it) }
            }.map { tuple ->
                val userDTO = tuple.t1
                val ns = tuple.t2
                BgGraph(userDTO.graphSettings).addEntries(ns)
            }
    }
}
