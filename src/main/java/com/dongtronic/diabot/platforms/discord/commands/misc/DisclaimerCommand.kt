package com.dongtronic.diabot.platforms.discord.commands.misc

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class DisclaimerCommand(category: Command.Category) : DiscordCommand(category, null) {

    private val logger = LoggerFactory.getLogger(DisclaimerCommand::class.java)

    init {
        this.name = "disclaimer"
        this.help = "Show the disclaimer for diabot"
        this.guildOnly = false
        this.ownerCommand = false
        this.hidden = false
        this.cooldown = 180
        this.cooldownScope = CooldownScope.CHANNEL
    }

    override fun execute(event: CommandEvent) {
        val text = this::class.java.classLoader.getResource("DISCLAIMER").readText()

        event.reply(text)
    }
}
