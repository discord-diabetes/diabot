package com.dongtronic.diabot.commands.nightscout

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.NightscoutDAO
import com.dongtronic.diabot.util.CommandUtils
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
import org.slf4j.LoggerFactory

class NightscoutAdminListCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(NightscoutAdminListCommand::class.java)

    init {
        this.name = "list"
        this.help = "List all configured Nightscout URLs"
        this.guildOnly = true
        this.ownerCommand = false
        this.aliases = arrayOf("l", "ls")
        this.category = category
        this.examples = arrayOf(this.parent!!.name + " list")
    }

    override fun execute(event: CommandEvent) {
        if(!CommandUtils.requireAdminChannel(event)) {
            return
        }

        logger.info("Listing all Nightscout URLs for ${event.author.name}")
        val users = NightscoutDAO.getInstance().listUsers()

        val builder = EmbedBuilder()

        builder.setTitle("Nightscout users")

        for ((user, value) in users) {
            val userId = user.substring(0, user.indexOf(":"))

            val username = event.jda.getUserById(userId).name


            builder.appendDescription("**$username** ($userId) -> $value\n")
        }

        event.reply(builder.build())
    }
}
