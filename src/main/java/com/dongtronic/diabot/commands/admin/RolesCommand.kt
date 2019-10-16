package com.dongtronic.diabot.commands.admin

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.Permission

class RolesCommand(category: Command.Category) : DiabotCommand(category, null) {

    init {
        this.name = "roles"
        this.help = "Get all roles in the server"
        this.guildOnly = true
        this.userPermissions = arrayOf(Permission.ADMINISTRATOR)
    }

    override fun execute(event: CommandEvent) {

        val guild = event.guild
        val roles = guild.roles

        val returned = StringBuilder()

        for (role in roles) {
            returned.append("\n").append(role.id).append(" - ").append(role.name)
        }

        event.reply(returned.toString())
    }
}
