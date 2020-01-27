package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.InfoDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import java.lang.Exception

class InfoListCommand(category: Category, parent: Command) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(InfoListCommand::class.java)

    init {
        this.name = "list"
        this.help = "List projects"
        this.guildOnly = false
        this.aliases = arrayOf("l", "ls")
        this.examples = arrayOf(this.getName())
    }

    override fun execute(event: CommandEvent) {
        try {
            val args = event.args.split("[^\\S\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toList()

            if (args.isNotEmpty()) {
                event.replyError("Valid syntax: `diabot info list`")
                return
            }

            val projects = InfoDAO.getInstance().listProjects()

            val builder = EmbedBuilder()

            builder.setTitle("Available Projects")
            builder.addField(MessageEmbed.Field("Help", "Use `diabot info [project]` to get information about a project", false))
            builder.setDescription(projects.sorted().joinToString("\n"))

            event.reply(builder.build())
        } catch (ex : Exception) {
            event.replyError(ex.message)
        }
    }
}