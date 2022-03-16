package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class ApplicationCommandListener(vararg val commands: ApplicationCommand) : ListenerAdapter() {
    private val logger = logger()
    private val commandMap: Map<String, ApplicationCommand>
    private val buttonIdMap: Map<String, ApplicationCommand>

    init {
        commandMap = HashMap()
        buttonIdMap = HashMap()

        commands.forEach { command ->
            if (commandMap.containsKey(command.commandName)) {
                throw IllegalStateException("Duplicate slash command handler configured for command ${command.commandName}: ${command.javaClass}")
            }

            commandMap[command.commandName] = command

            command.buttonIds.forEach { buttonId ->
                if (buttonIdMap.containsKey(buttonId)) {
                    throw IllegalStateException("Duplicate application button ID for command ${command.commandName}: $buttonId")
                }

                buttonIdMap[buttonId] = command
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commandClass = commandMap[event.name]

        if (commandClass != null) {
            commandClass.execute(event)
        } else {
            event.reply("No class specified for this command. Please open an issue: <https://github.com/reddit-diabetes/diabot>").setEphemeral(true).queue()
            logger.error("No slash command class for command: ${event.name}")
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val commandClass = buttonIdMap[event.componentId]

        if (commandClass != null) {
            commandClass.execute(event)
        } else {
            event.reply("No class specified for this command. Please open an issue: <https://github.com/reddit-diabetes/diabot>").setEphemeral(true).queue()
            logger.error("No Application command class for button: ${event.componentId}")
        }
    }

}
