package com.dongtronic.diabot.platforms.discord.commands.info

import com.dongtronic.diabot.data.redis.InfoDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class InfoDeleteCommand(category: Command.Category, parent: Command) : DiscordCommand(category, parent) {
    private val logger = logger()

    init {
        this.name = "delete"
        this.help = "Delete project information"
        this.guildOnly = true
        this.aliases = arrayOf("d", "del", "rem", "remove")
        this.userPermissions = arrayOf(Permission.MANAGE_CHANNEL)
        this.examples = arrayOf(this.getName() + " openaps")
    }

    override fun execute(event: CommandEvent) {
        try {
            // This command may exclusively be run on the official Diabetes server.
            if (event.guild.id != System.getenv("HOME_GUILD_ID")) {
                event.replyError(System.getenv("HOME_GUILD_MESSAGE"))
                return
            }

            val args = event.args.split("[^\\S\r\n]".toRegex()).dropLastWhile { it.isEmpty() }.toList()

            if (args.size != 1) {
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