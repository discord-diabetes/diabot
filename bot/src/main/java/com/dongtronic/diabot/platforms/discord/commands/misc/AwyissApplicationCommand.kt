package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.logic.`fun`.Awyisser
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import org.bson.internal.Base64
import reactor.core.scheduler.Schedulers

class AwyissApplicationCommand : ApplicationCommand {
    private val commandArgSfw = "sfw"
    private val commandArgValue = "value"

    override val commandName: String = "awyiss"
    override val buttonIds: Set<String> = emptySet()

    override fun config(): CommandData {
        return CommandData(commandName, "Generate Awyiss meme image")
                .addOption(OptionType.STRING, commandArgValue, "Awyiss string (max 30 chars)", true)
                .addOption(OptionType.BOOLEAN, commandArgSfw, "Safe for work", false)
    }

    override fun execute(event: SlashCommandEvent) {
        val stringValue = event.getOption(commandArgValue)!!.asString
        val sfwValue = event.getOption(commandArgSfw)?.asBoolean ?: true

        event.deferReply(true).queue()

        Awyisser.generate(stringValue, sfwValue)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe({ imageUrl ->
                    val imageStream = Base64.decode(imageUrl.removePrefix("data:image/png;base64,"))
                    event.reply("Awyiss for $stringValue").addFile(imageStream, "awyiss.png").submit()
                }, {
                    if (it is RequestStatusException && it.status == 500) {
                        logger().warn("Encountered server error while generating Awyiss: ${it.message}")
                        event.reply("Server error occurred.").setEphemeral(true).submit()
                        return@subscribe
                    }

                    logger().warn("Encountered error while generating Awyiss: ${it.message}")
                    event.reply("Something went wrong: " + it.message).setEphemeral(true).submit()
                })
    }

    override fun execute(event: ButtonClickEvent) {
        TODO("Not yet implemented")
    }
}
