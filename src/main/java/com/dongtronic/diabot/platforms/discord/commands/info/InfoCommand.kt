package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.data.InfoDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.Logger
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class InfoCommand(category: Category) : DiscordCommand(category, null) {

    private val logger by Logger()

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