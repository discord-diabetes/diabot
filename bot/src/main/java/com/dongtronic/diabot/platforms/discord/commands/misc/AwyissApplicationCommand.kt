package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.logic.`fun`.Awyisser
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
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

        event.deferReply().queue()

        Awyisser.generate(stringValue, sfwValue)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe({ imageUrl ->
                    val imageStream = Base64.decode(imageUrl.removePrefix("data:image/png;base64,"))
                    event.hook.editOriginal(imageStream, "awyiss.png").queue()
                }, {
                    if (it is RequestStatusException && it.status == 500) {
                        replyError(event, it, "Server error occurred.")
                        return@subscribe
                    }

                    replyError(event, it, "Something went wrong: " + it.message)
                })
    }

    override fun execute(event: ButtonClickEvent) {
        TODO("Not yet implemented")
    }
}
