package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.data.mongodb.ProjectDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class InfoSetCommand(category: Command.Category, parent: Command) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "set"
        this.help = "Set project information"
        this.guildOnly = true
        this.aliases = arrayOf("s")
        this.userPermissions = arrayOf(Permission.MANAGE_CHANNEL)
        this.examples = arrayOf(this.getName() + " openaps openaps is a project for ...")
    }

    override fun execute(event: CommandEvent) {
        // This command may exclusively be run on the official Diabetes server.
        if (event.guild.id != System.getenv("HOME_GUILD_ID")) {
            event.replyError(System.getenv("HOME_GUILD_MESSAGE"))
            return
        }

        val args = event.args.split("[^\\S\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toList()

        if (args.isEmpty() || args.size < 2) {
            event.replyError("Valid syntax: `diabot info set [project] [description]`")
            return
        }

        val project = args[0]

        val description = args.minus(project).joinToString(" ")

        ProjectDAO.instance.setInfo(project, description).subscribe({
            if (it.upsertedId == null) {
                event.replySuccess("Description for `$project` updated")
            } else {
                event.replySuccess("Added project `$project`")
            }
        }, {
            logger.warn("Could not set project description", it)
            event.replyError(it.message)
        })
    }
}