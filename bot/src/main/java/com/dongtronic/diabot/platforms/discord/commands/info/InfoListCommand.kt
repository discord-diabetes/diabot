package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.data.mongodb.ProjectDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed

class InfoListCommand(category: Category, parent: Command) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "list"
        this.help = "List projects"
        this.guildOnly = false
        this.aliases = arrayOf("l", "ls")
        this.examples = arrayOf(this.getName())
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("[^\\S\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toList()

        if (args.isNotEmpty()) {
            event.replyError("Valid syntax: `diabot info list`")
            return
        }

        ProjectDAO.instance.listProjects().collectSortedList().subscribe({ dtos ->
            val projects = dtos.map { it.name }
            val builder = EmbedBuilder()

            builder.setTitle("Available Projects")
            builder.addField(MessageEmbed.Field("Help", "Use `diabot info [project]` to get information about a project", false))
            builder.setDescription(projects.joinToString("\n"))

            event.reply(builder.build())
        }, {
            if (it is NoSuchElementException) {
                event.reply("No projects are available.")
            } else {
                logger.warn("Could not get list of projects", it)
                event.replyError(it.message)
            }
        })
    }
}