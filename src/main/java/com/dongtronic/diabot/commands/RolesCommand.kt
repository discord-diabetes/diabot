package com.dongtronic.diabot.commands

import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder
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

        val builder = EmbedBuilder()

        builder.setTitle("Roles for ${event.guild.name}")

        for (role in roles) {
            builder.addField(role.name, role.id, true)
        }

        event.reply(builder.build())
    }
}
