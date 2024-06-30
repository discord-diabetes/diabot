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
import com.dongtronic.nightscout.data.NightscoutDTO
import com.dongtronic.nightscout.exceptions.NoNightscoutDataException
import com.fasterxml.jackson.core.JsonProcessingException
import dev.minn.jda.ktx.coroutines.await
import kotlinx.coroutines.reactor.awaitSingle
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
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

class NightscoutGraphApplicationCommand : ApplicationCommand {
    override val commandName: String = "graph"
    private val logger = logger()
    private val cooldowns = mutableMapOf<String, Long>()

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val cooldownSeconds = getCooldown(event.user.id)
        if (cooldownSeconds != null) {
            val plural = if (abs(cooldownSeconds) != 1L) "s" else ""
            event.reply("This command is currently on a cooldown. You can use it again in $cooldownSeconds second$plural")
                .setEphemeral(true)
                .queue()
            return
        }

        val hours = event.getOption("hours")?.asLong

        if (hours != null && (hours < 1 || hours > 24)) {
            event.reply("The number of hours must be between 1 and 24")
                .setEphemeral(true)
                .queue()
            return
        }

        try {
            val enabled = !event.isFromGuild ||
                GraphDisableDAO.instance.getGraphEnabled(event.guild!!.id).awaitSingle()

            if (!enabled) {
                event.reply("Nightscout graphs are disabled in this guild").setEphemeral(true).queue()
                return
            }

            event.deferReply(false).queue()

            val chart = getDataSet(event.user.id, hours).awaitSingle()
            val imageBytes = chart.getBitmapBytes(BitmapFormat.PNG)
            event.hook.editOriginalAttachments(FileUpload.fromData(imageBytes, "graph.png")).submit().await()
            applyCooldown(event.user.id)
        } catch (e: Exception) {
            logger.error("Error generating NS graph for ${event.user}")
            if (e is NightscoutFetchException) {
                event.hook.editOriginal(NightscoutCommand.handleGrabError(e.originalException, event.user, e.userDTO)).queue()
            } else {
                event.hook.editOriginal(NightscoutCommand.handleError(e)).queue()
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

    override fun config(): CommandData {
        return Commands.slash(commandName, "Generate a graph from Nightscout")
            .addOption(OptionType.INTEGER, "hours", "Amount of hours to display on graph")
    }

    private fun getDataSet(senderId: String, hours: Long?): Mono<BgGraph> {
        return NightscoutDAO.instance.getUser(senderId)
            .onErrorMap(NoSuchElementException::class) { UnconfiguredNightscoutException() }
            .zipWhen { userDTO ->
                if (userDTO.url == null) {
                    return@zipWhen Mono.error<NightscoutDTO>(UnconfiguredNightscoutException())
                }

                val ns = Nightscout(userDTO.url, userDTO.token)

                val time = (hours ?: userDTO.graphSettings.hours)
                    .let { Duration.ofHours(it) }

                // calculate the amount of readings there should be.
                // assume 1 reading every 5 minutes and a minimum of 1 reading
                val count = max(time.toMinutes() / 5, 1).toInt()
                val startTime = Instant.now()
                    .minus(time)
                    .toEpochMilli()
                    .toString()
                val findParam = EntriesParameters()
                    .find("sgv", operator = MongoOperator.exists)
                    .find("date", startTime, MongoOperator.gte)
                    .count(count)
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
