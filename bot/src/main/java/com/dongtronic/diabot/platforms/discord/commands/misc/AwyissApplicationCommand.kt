package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.exceptions.RequestStatusException
import com.dongtronic.diabot.logic.`fun`.Awyisser
import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.github.kaktushose.jda.commands.annotations.interactions.Command
import com.github.kaktushose.jda.commands.annotations.interactions.Interaction
import com.github.kaktushose.jda.commands.annotations.interactions.Param
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import reactor.core.scheduler.Schedulers
import java.util.*

@Interaction
class AwyissApplicationCommand : ApplicationCommand {
    @Command("awyiss", desc = "Generate Awyiss meme image")
    fun awyiss(
        event: CommandEvent,
        @Param("Awyiss string (max 30 chars)")
        text: String,
        @Param("Safe for work", optional = true)
        sfw: Boolean?
    ) {
        event.deferReply(false)

        Awyisser.generate(text, sfw ?: true)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe({ imageUrl ->
                val imageStream = Base64.getDecoder().decode(imageUrl.removePrefix("data:image/png;base64,"))
                event.reply(MessageCreateData.fromFiles(FileUpload.fromData(imageStream, "awyiss.png")))
            }, {
                if (it is RequestStatusException && it.status == 500) {
                    replyError(event, it, "Server error occurred.")
                    return@subscribe
                }

                replyError(event, it, "Something went wrong: " + it.message)
            })
    }
}
