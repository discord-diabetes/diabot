package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.data.mongodb.ProjectDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class InfoCommand(category: Category) : DiscordCommand(category, null) {

    private val logger = logger()

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

        ProjectDAO.instance.getProject(args[0]).subscribe({
            val builder = EmbedBuilder()

            builder.setTitle(it.name)
            builder.setDescription(it.text)

            event.reply(builder.build())
        }, {
            if (it is NoSuchElementException) {
                 event.replyError("Could not find project info for ${args[0]}")
            } else {
                logger.warn("Could not retrieve project info", it)
                event.replyError(it.message)
            }
        })
    }
}