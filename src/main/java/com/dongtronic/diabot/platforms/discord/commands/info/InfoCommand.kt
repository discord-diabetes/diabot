package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.InfoDAO
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import org.slf4j.LoggerFactory

class InfoCommand(category: Category) : DiabotCommand(category, null) {

    private val logger = LoggerFactory.getLogger(InfoCommand::class.java)

    init {
        this.name = "info"
        this.help = "Project Information. Administrators can add new projects"
        this.guildOnly = false
        this.aliases = arrayOf("i")
        this.examples = arrayOf()
        this.children = arrayOf(
                InfoSetCommand(category, this),
                InfoListCommand(category, this),
                InfoDeleteCommand(category, this))
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            // List all available projects
            event.replyError("Please specify a command")
            return
        }

        try {
            val project = args[0]

            val text = InfoDAO.getInstance().getProjectText(project)

            val builder = EmbedBuilder()

            builder.setTitle(InfoDAO.getInstance().formatProject(project))
            builder.setDescription(text)

            event.reply(builder.build())
        } catch (ex: Exception) {
            event.replyError(ex.message)
        }
    }
}