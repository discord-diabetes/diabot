package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class OwnerCommand(category: Category) : DiscordCommand(category, null) {

    init {
        this.name = "owner"
        this.help = "Say hi to the bot owner"
        this.guildOnly = false
        this.ownerCommand = true
        this.aliases = arrayOf("hi", "hello", "sup")
        this.hidden = true
    }

    override fun execute(event: CommandEvent) {
        val nickname = event.guild.getMember(event.author)!!.effectiveName

        event.reply(":wave: Hello $nickname :)\nThanks for making me :D")
    }

}