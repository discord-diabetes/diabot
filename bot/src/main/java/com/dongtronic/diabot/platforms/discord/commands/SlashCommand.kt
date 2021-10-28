package com.dongtronic.diabot.platforms.discord.commands

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData

interface SlashCommand {
    val commandName: String

    fun execute(event: SlashCommandEvent)

    fun config(): CommandData
}
