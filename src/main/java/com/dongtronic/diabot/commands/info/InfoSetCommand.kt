package com.dongtronic.diabot.commands.info

import com.dongtronic.diabot.commands.DiabotCommand
import com.dongtronic.diabot.data.InfoDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission
import org.slf4j.LoggerFactory
import java.lang.Exception

class InfoSetCommand(category: Command.Category, parent: Command) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(InfoSetCommand::class.java)

    init {
        this.name = "set"
        this.help = "Set project information"
        this.guildOnly = true
        this.aliases = arrayOf("s")
        this.userPermissions = arrayOf(Permission.MANAGE_CHANNEL)
        this.examples = arrayOf(this.getName() + " openaps openaps is a project for ...")
    }

    override fun execute(event: CommandEvent) {
        try {
            // This command may exclusively be run on the official Diabetes server.
            if (event.guild.id != "257554742371155998") {
                event.replyError("This command can only be executed on the official r/Diabetes Discord server. https://discord.gg/diabetes")
                return
            }

            val args = event.args.split("[^\\S\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toList()

            if (args.isEmpty() || args.size < 2) {
                event.replyError("Valid syntax: `diabot info set [project] [description]`")
                return
            }

            val project = args[0]

            if (project.toUpperCase() == "PROJECT" || project.toUpperCase() == "PROJECTS") {
                // protect database key
                throw IllegalArgumentException("Project name $project is forbidden")
            }

            val description = args.reversed().dropLast(1).reversed().joinToString(" ")

            InfoDAO.getInstance().setProjectText(project, description)

            event.replySuccess("Description for $project updated")
        } catch (ex : Exception) {
            event.replyError(ex.message)
        }
    }
}