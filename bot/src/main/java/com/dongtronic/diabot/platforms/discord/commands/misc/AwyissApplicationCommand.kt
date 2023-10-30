package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.logic.`fun`.Awyisser
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import org.bson.internal.Base64
import reactor.core.scheduler.Schedulers

class AwyissApplicationCommand : ApplicationCommand {
    private val commandArgSfw = "sfw"
    private val commandArgValue = "value"

    override val commandName: String = "awyiss"

    override fun config(): CommandData {
        return Commands.slash(commandName, "Generate Awyiss meme image")
                .addOption(OptionType.STRING, commandArgValue, "Awyiss string (max 30 chars)", true)
                .addOption(OptionType.BOOLEAN, commandArgSfw, "Safe for work", false)
    }

    override suspend fun execute(event: SlashCommandInteractionEvent) {
        val stringValue = event.getOption(commandArgValue)!!.asString
        val sfwValue = event.getOption(commandArgSfw)?.asBoolean ?: true

        event.deferReply().queue()

        Awyisser.generate(stringValue, sfwValue)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe({ imageUrl ->
                    val imageStream = Base64.decode(imageUrl.removePrefix("data:image/png;base64,"))
                    event.hook.editOriginalAttachments(FileUpload.fromData(imageStream, "awyiss.png")).queue()
                }, {
                    if (it is RequestStatusException && it.status == 500) {
                        replyError(event, it, "Server error occurred.")
                        return@subscribe
                    }

                    replyError(event, it, "Something went wrong: " + it.message)
                })
    }
}
