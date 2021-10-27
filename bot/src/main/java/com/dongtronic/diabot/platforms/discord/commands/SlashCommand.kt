package com.dongtronic.diabot.platforms.discord.commands

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent

abstract class SlashCommand {
    abstract val commandName: String

    abstract fun execute(event: SlashCommandEvent)
}