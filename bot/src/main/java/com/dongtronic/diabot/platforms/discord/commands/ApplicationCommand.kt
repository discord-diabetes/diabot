package com.dongtronic.diabot.platforms.discord.commands

import com.dongtronic.diabot.util.logger
import com.github.kaktushose.jda.commands.annotations.interactions.Button
import com.github.kaktushose.jda.commands.dispatching.events.interactions.CommandEvent
import com.github.kaktushose.jda.commands.dispatching.events.interactions.ComponentEvent

interface ApplicationCommand {
    @Button(value = "Report bug", link = "https://github.com/discord-diabetes/diabot/issues/new?assignees=&labels=bug&template=bug_report.md")
    fun reportBugButton(event: ComponentEvent) {}

    fun replyError(event: CommandEvent, exception: Throwable, message: String) {
        event.with().ephemeral(true).editReply(true).components("reportBugButton").reply(message)
        logger().error(exception.message, exception)
    }

    fun replyError(event: ComponentEvent, exception: Throwable, message: String) {
        event.with().ephemeral(true).editReply(true).components("reportBugButton").reply(message)
        logger().error(exception.message, exception)
    }
}
