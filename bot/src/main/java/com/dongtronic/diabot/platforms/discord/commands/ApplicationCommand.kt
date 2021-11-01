package com.dongtronic.diabot.platforms.discord.commands

import com.dongtronic.diabot.util.logger
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.components.Button

interface ApplicationCommand {
    val commandName: String
    val buttonIds: Set<String>

    fun execute(event: SlashCommandEvent)

    fun execute(event: ButtonClickEvent)

    fun config(): CommandData

    fun replyError(event: SlashCommandEvent, exception: Throwable, message: String) {
        event.reply(message).addActionRow(
                Button.link("https://github.com/reddit-diabetes/diabot/issues/new?assignees=&labels=bug&template=bug_report.md", "Report bug")
        ).setEphemeral(true).queue()
        logger().error(exception.message, exception)
    }

    fun replyError(event: ButtonClickEvent, exception: Throwable, message: String) {
        event.editMessage(message).setActionRow(
                Button.link("https://github.com/reddit-diabetes/diabot/issues/new?assignees=&labels=bug&template=bug_report.md", "Report bug")
        ).queue()
        logger().error(exception.message, exception)
    }
}
