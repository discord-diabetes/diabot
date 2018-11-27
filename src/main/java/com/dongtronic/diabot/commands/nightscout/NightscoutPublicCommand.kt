package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.NicknameUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class NightscoutPublicCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutPublicCommand::class.java)

    init {
        this.name = "public"
        this.help = "Make your Nightscout data public or private"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("pub", "p")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " public on")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+")

        if(args.isEmpty()) {
            event.replyError("Please specify public status (true/false)")
        }

        val mode = args[0].toUpperCase()

        if (mode == "TRUE" || mode == "T" || mode == "YES" || mode == "Y") {
            NightscoutDAO.getInstance().setNightscoutPublic(event.author, true)
            event.reply("Nightscout data for ${NicknameUtils.determineAuthorDisplayName(event)} set to public")
        } else {
            NightscoutDAO.getInstance().setNightscoutPublic(event.author, false)
            event.reply("Nightscout data for ${NicknameUtils.determineAuthorDisplayName(event)} set to private")
        }
    }
}
