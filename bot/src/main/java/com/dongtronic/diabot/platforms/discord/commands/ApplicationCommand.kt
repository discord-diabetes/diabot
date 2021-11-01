package com.dongtronic.diabot.platforms.discord.commands

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface ApplicationCommand {
    val commandName: String
    val buttonIds: Set<String>

    fun execute(event: SlashCommandEvent)

    fun execute(event: ButtonClickEvent)

    fun config(): CommandData
}
