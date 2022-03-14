package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.mongodb.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User
import reactor.core.publisher.Mono
import java.util.*

class NightscoutSetDisplayCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {
    companion object {
        val enabledOptions = arrayOf("title", "trend", "cob", "iob", "avatar")
        val validOptions = enabledOptions.plus(arrayOf("simple", "none"))
    }

    private val logger = logger()

    init {
        this.name = "display"
        this.help = "Set display options for NS cards"
        this.guildOnly = false
        this.ownerCommand = false
        this.aliases = arrayOf("setdisplay")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " display trend cob avatar", this.parent.name + " display reset (to reset)")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("[\\s,]+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            event.reply("Possible display options: ${formatOptions()}")
            return
        }

        val updateDisplay = if (args.size != 1 || args[0] != "reset") {
            // remove duplicates via Set
            var options = args.toSet()
            // Prioritize `none` option over any others provided
            if (options.contains("none")) {
                options = setOf("none")
            }

            // verify options and set
            for (opt in options) {
                if (!validOptions.contains(opt.lowercase())) {
                    event.replyError("Unsupported display option provided (`$opt`), use `diabot nightscout display` to see possible options")
                    return
                }
            }

            setNightscoutDisplay(event.author, *options.toTypedArray())
        } else {
            // provide no args for reset
            setNightscoutDisplay(event.author)
        }

        NicknameUtils.determineAuthorDisplayName(event).subscribe { nickname ->
            updateDisplay.subscribe({ newOptions ->
                if (newOptions.any { it == "reset" }) {
                    event.replySuccess("Reset Nightscout display options for $nickname")
                } else {
                    event.replySuccess("Nightscout display options for $nickname set to: ${formatOptions(newOptions)}")
                }
            }, {
                logger.warn("Error while setting NS display options", it)
                event.replyError("Could not set Nightscout display options for $nickname")
            })
        }
    }

    private fun setNightscoutDisplay(user: User, vararg options: String): Mono<List<String>> {
        return NightscoutDAO.instance.updateDisplay(user.id, null, *options)
    }

    private fun formatOptions(options: List<String> = validOptions.toList()): String {
        return options.joinToString(" ", "`", "`")
    }
}
