package com.dongtronic.diabot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.Permission

class RolesCommand(category: Command.Category) : DiabotCommand() {

    init {
        this.name = "roles"
        this.help = "Get all roles in the server"
        this.guildOnly = true
        this.category = category
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {

        val guild = event.guild

        val roles = guild.roles

        val returned = StringBuilder().append("```")

        for (role in roles) {
            returned.append("\n").append(role.id).append(" - ").append(role.name)
        }

        returned.append("\n```")

        event.reply(returned.toString())
    }
}
