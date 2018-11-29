package com.dongtronic.diabot.commands.misc

import com.dongtronic.diabot.commands.DiabotCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.core.EmbedBuilder

class GithubCommand(category: Command.Category) : DiabotCommand(category, null) {

    init {
        this.name = "github"
        this.help = "Get the GitHub link for Diabot"
        this.hidden = false
    }

    override fun execute(event: CommandEvent) {
        val builder = EmbedBuilder()

        builder.setTitle("Diabot GitHub")

        builder.setDescription("https://github.com/reddit-diabetes/diabot-discord")

        builder.addField("Issues", "https://github.com/reddit/diabetes/diabot/discord/issues", true)
        builder.addField("Bug report", "https://github.com/reddit-diabetes/diabot-discord/issues/new?template=bug_report.md", true)
        builder.addField("Feature request", "https://github.com/reddit-diabetes/diabot-discord/issues/new?template=feature_request.md", true)

        builder.setColor(java.awt.Color.blue)

        event.reply(builder.build())
    }
}
