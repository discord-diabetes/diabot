package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent
import net.dv8tion.jda.api.EmbedBuilder

class GithubCommand(category: Category) : DiscordCommand(category, null) {

    init {
        this.name = "github"
        this.help = "Get the GitHub link for Diabot"
        this.hidden = false
        this.guildOnly = false
    }

    override fun execute(event: CommandEvent) {
        val builder = EmbedBuilder()

        builder.setTitle("Diabot GitHub")

        builder.setDescription("https://github.com/discord-diabetes/diabot")

        builder.addField("Issues", "https://github.com/discord-diabetes/diabot/issues", true)
        builder.addField("Bug report", "https://github.com/discord-diabetes/diabot/issues/new?template=bug_report.md", true)
        builder.addField("Feature request", "https://github.com/discord-diabetes/diabot/issues/new?template=feature_request.md", true)

        builder.setColor(java.awt.Color.blue)

        event.reply(builder.build())
    }
}
