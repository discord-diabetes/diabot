package com.dongtronic.diabot.platforms.discord.commands.admin

import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.jagrosh.jdautilities.command.CommandEvent

class OwnerCommand(category: Category) : DiscordCommand(category, null) {

    init {
        this.name = "hi"
        this.help = "Say hi"
        this.guildOnly = false
        this.ownerCommand = false
        this.aliases = arrayOf("owner", "hello", "sup")
        this.hidden = true
    }

    override fun execute(event: CommandEvent) {
        val nickname = event.guild.getMember(event.author)!!.effectiveName

        if (event.isOwner) {
            event.reply(":wave: Hello $nickname :)\nThanks for making me :heart:")
        } else {
            event.reply(":wave: Hello $nickname")
        }
    }

}