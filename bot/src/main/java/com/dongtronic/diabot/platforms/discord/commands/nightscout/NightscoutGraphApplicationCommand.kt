package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.GraphDisableDAO
import com.dongtronic.diabot.exceptions.NightscoutFetchException
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.util.logger
import dev.minn.jda.ktx.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.knowm.xchart.BitmapEncoder
import java.time.Duration
import kotlin.math.abs

class NightscoutGraphApplicationCommand : ApplicationCommand {
    override val commandName: String = "graph"
    override val buttonIds: Set<String> = emptySet()
    private val logger = logger()
    private val cooldowns = mutableMapOf<String, Long>()

    override fun execute(event: SlashCommandEvent) {
        val cooldownSeconds = getCooldown(event.user.id)
        if (cooldownSeconds != null) {
            val plural = if (abs(cooldownSeconds) != 1L) "s" else ""
            event.reply("This command is currently on a cooldown. You can use it again in $cooldownSeconds second$plural")
                    .setEphemeral(true)
                    .queue()
            return
        }

        runBlocking {
            launch {
                val hook = event.deferReply().submit()
                try {
                    val enabled = !event.isFromGuild
                            || GraphDisableDAO.instance.getGraphEnabled(event.guild!!.id).awaitSingle()

                    if (!enabled) {
                        hook.await()
                                .setEphemeral(true)
                                .editOriginal("Nightscout graphs are disabled in this guild")
                                .await()
                        return@launch
                    }

                    val chart = NightscoutGraphCommand.getDataSet(event.user.id).awaitSingle()
                    val imageBytes = BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG)
                    hook.await().editOriginal(imageBytes, "graph.png").submit().await()
                    applyCooldown(event.user.id)
                } catch (e: Exception) {
                    logger.error("Error generating NS graph for ${event.user}")
                    if (e is NightscoutFetchException) {
                        hook.await()
                                .editOriginal(NightscoutCommand.handleGrabError(e.originalException, event.user, e.userDTO))
                                .queue()
                    } else {
                        hook.await().editOriginal(NightscoutCommand.handleError(e)).queue()
                    }
                }
            }
        }
    }

    private fun getCooldown(id: String): Long? {
        cleanCooldowns()
        val time = cooldowns[id]

        // find time difference, then convert ms -> s
        return time?.let {
            (abs(it - System.currentTimeMillis())) / 1000
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

    override fun execute(event: ButtonClickEvent) {}

    override fun config(): CommandData {
        return CommandData(commandName, "Generate a graph from Nightscout")
    }
}
