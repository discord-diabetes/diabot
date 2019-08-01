package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.NicknameUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.entities.User
import org.slf4j.LoggerFactory

class NightscoutSetDisplayCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {
    companion object {
        val validOptions = arrayOf("none", "title", "trend", "cob", "iob", "avatar")
    }
    private val logger = LoggerFactory.getLogger(NightscoutSetDisplayCommand::class.java)

    init {
        this.name = "display"
        this.help = "Set display options for NS cards"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("setdisplay")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " display trend cob avatar", this.parent.name + " display reset (to reset)")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val nickname = NicknameUtils.determineAuthorDisplayName(event)

        when {
            args.size == 1 && args[0] == "reset" -> {
                resetNightscoutDisplay(event.author)
                event.replySuccess("Reset Nightscout display options for $nickname")
            }

            args.isNotEmpty() -> {
                var options = args
                // Prioritize `none` option over any others provided
                if(options.contains("none")) {
                    options = arrayOf("none")
                }

                // verify options and set
                for(opt in options) {
                    if(!validOptions.contains(opt.toLowerCase())) {
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
        NightscoutDAO.getInstance().setNightscoutDisplay(user, "")
    }
}
