package com.dongtronic.diabot.platforms.discord.commands

import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.components.buttons.Button

interface ApplicationCommand {
    val commandName: String
    val buttonIds: Set<String>

    fun execute(event: SlashCommandInteractionEvent)

    fun execute(event: ButtonInteractionEvent)

    fun config(): CommandData

    fun replyError(event: SlashCommandInteractionEvent, exception: Throwable, message: String) {
        val reportButton = Button.link("https://github.com/reddit-diabetes/diabot/issues/new?assignees=&labels=bug&template=bug_report.md", "Report bug")

        if (event.isAcknowledged) {
            event.hook.setEphemeral(true).editOriginal(message).setActionRow(reportButton).queue()
        } else {
            event.reply(message).addActionRow(reportButton).setEphemeral(true).queue()
        }
        logger().error(exception.message, exception)
    }

    fun replyError(event: ButtonInteractionEvent, exception: Throwable, message: String) {
        event.editMessage(message).setActionRow(
                Button.link("https://github.com/reddit-diabetes/diabot/issues/new?assignees=&labels=bug&template=bug_report.md", "Report bug")
        ).queue()
        logger().error(exception.message, exception)
    }
}
