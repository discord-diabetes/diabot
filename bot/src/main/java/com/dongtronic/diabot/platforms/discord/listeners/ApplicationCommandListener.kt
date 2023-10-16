package com.dongtronic.diabot.platforms.discord.listeners

import com.dongtronic.diabot.platforms.discord.commands.ApplicationCommand
import com.dongtronic.diabot.util.logger
import dev.minn.jda.ktx.events.CoroutineEventListener
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent

class ApplicationCommandListener(vararg val commands: ApplicationCommand) : CoroutineEventListener {
    private val logger = logger()
    private val commandMap: Map<String, ApplicationCommand>

    init {
        commandMap = HashMap()

        commands.forEach { command ->
            if (commandMap.containsKey(command.commandName)) {
                throw IllegalStateException("Duplicate slash command handler configured for command ${command.commandName}: ${command.javaClass}")
            }

            commandMap[command.commandName] = command
        }
    }

    override suspend fun onEvent(event: GenericEvent) {
        when (event) {
            is SlashCommandInteractionEvent -> onSlashCommandInteraction(event)
            is ButtonInteractionEvent -> onButtonInteraction(event)
            is ModalInteractionEvent -> onModalInteraction(event)
        }
    }

    suspend fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val commandClass = commandMap[event.name]

        if (commandClass != null) {
            commandClass.execute(event)
        } else {
            event.reply("No class specified for this command. Please open an issue: <https://github.com/discord-diabetes/diabot>").setEphemeral(true).queue()
            logger.error("No slash command class for command: ${event.name}")
        }
    }

    suspend fun onButtonInteraction(event: ButtonInteractionEvent) {
        val name = event.componentId.split(':').firstOrNull()
        val commandClass = commandMap[name]

        if (commandClass == null || !commandClass.execute(event)) {
            event.reply("No class specified for this command. Please open an issue: <https://github.com/discord-diabetes/diabot>").setEphemeral(true).queue()
            logger.error("No Application command class for button: ${event.componentId}")
        }
    }

    suspend fun onModalInteraction(event: ModalInteractionEvent) {
        val name = event.modalId.split(':').firstOrNull()
        val commandClass = commandMap[name]

        if (commandClass == null || !commandClass.execute(event)) {
            event.reply("No class specified for this modal. Please open an issue: <https://github.com/discord-diabetes/diabot>").setEphemeral(true).queue()
            logger.error("No Application command class for modal: ${event.modalId}")
        }
    }
}
