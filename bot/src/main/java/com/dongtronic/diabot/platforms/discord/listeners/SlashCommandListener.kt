package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.platforms.discord.commands.SlashCommand
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class SlashCommandListener(vararg val commands: SlashCommand) : ListenerAdapter() {
    private val logger = logger()
    private val commandMap: Map<String, SlashCommand>

    init {
        commandMap = HashMap()

        commands.forEach { command ->
            if (commandMap.containsKey(command.commandName)) {
                throw IllegalStateException("Duplicate slash command handler configured for command ${command.commandName}: ${command.javaClass}")
            }

            commandMap[command.commandName] = command
        }
    }

    override fun onSlashCommand(event: SlashCommandEvent) {
        val commandClass = commandMap[event.name]

        if (commandClass != null ) {
            commandClass.execute(event)
        } else {
            event.reply("No class specified for this command. Please open an issue: <https://github.com/reddit-diabetes/diabot>").setEphemeral(true).queue()
            logger.error("No slash command class for command: ${event.name}")
        }
    }

}
