package com.dongtronic.diabot.commands.info

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.InfoDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory
import java.lang.Exception

class InfoDeleteCommand(category: Command.Category, parent: Command) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(InfoDeleteCommand::class.java)

    init {
        this.name = "delete"
        this.help = "Delete project informaiton"
        this.guildOnly = true
        this.aliases = arrayOf("d", "del", "rem", "remove")
        this.userPermissions = arrayOf(Permission.MANAGE_CHANNEL)
        this.examples = arrayOf(this.getName() + " openaps")
    }

    override fun execute(event: CommandEvent) {
        try {
            // This command may exclusively be run on the official Diabetes server.
            if (event.guild.id != "257554742371155998") {
                event.replyError("This command can only be executed on the official r/Diabetes Discord server. https://discord.gg/diabetes")
                return
            }

            val args = event.args.split("[^\\S\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toList()

            if (args.size != 1) {
                // List all available projects
                event.replyError("Valid syntax: `diabot info delete [project]`")
                return
            }

            val project = args[0]

            InfoDAO.getInstance().removeProject(project)

            event.replySuccess("Deleted project info for $project")
        } catch (ex : Exception) {
            event.replyError(ex.message)
        }
    }
}