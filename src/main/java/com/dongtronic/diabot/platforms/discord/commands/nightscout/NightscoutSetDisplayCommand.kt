package com.dongtronic.diabot.platforms.discord.commands.nightscout

import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.platforms.discord.utils.NicknameUtils
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.entities.User

class NightscoutSetDisplayCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {
    companion object {
        val enabledOptions = arrayOf("title", "trend", "cob", "iob", "avatar")
        val validOptions = enabledOptions.plus(arrayOf("simple", "none"))
    }

    private val logger by Logger()

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
        val nickname = NicknameUtils.determineAuthorDisplayName(event)

        when {
            args.size == 1 && args[0] == "reset" -> {
                resetNightscoutDisplay(event.author)
                event.replySuccess("Reset Nightscout display options for $nickname")
            }

            args.isNotEmpty() -> {
                var options = args
                // Prioritize `none` option over any others provided
                if (options.contains("none")) {
                    options = arrayOf("none")
                }

                // verify options and set
                for (opt in options) {
                    if (!validOptions.contains(opt.toLowerCase())) {
                        event.replyError("Unsupported display option provided, use `diabot nightscout display` to see possible options")
                        return
                    }
                }

                setNightscoutDisplay(event.author, options.joinToString(" "))
                event.replySuccess("Nightscout display options for $nickname set to: " +
                        options.joinToString(" ", "`", "`"))
            }

            else -> event.reply("Possible display options: " +
                    validOptions.joinToString("`, `", "`", "`"))
        }
    }

    private fun setNightscoutDisplay(user: User, options: String) {
        NightscoutDAO.getInstance().setNightscoutDisplay(user, options)
    }

    private fun resetNightscoutDisplay(user: User) {
        NightscoutDAO.getInstance().removeNightscoutDisplay(user)
    }
}
