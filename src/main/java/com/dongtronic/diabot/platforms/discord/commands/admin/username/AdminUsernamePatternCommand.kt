package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.data.mongodb.NameRuleDAO
import com.dongtronic.diabot.platforms.discord.commands.DiscordCommand
import com.dongtronic.diabot.util.logger
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent

class AdminUsernamePatternCommand(category: Command.Category, parent: Command?) : DiscordCommand(category, parent) {

    private val logger = logger()

    init {
        this.name = "pattern"
        this.help = "Set or view username enforcement pattern"
        this.guildOnly = true
        this.aliases = arrayOf("p", "pat", "s", "set")
    }

    override fun execute(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            getPattern(event)
        } else {
            setPattern(event)
        }
    }

    private fun setPattern(event: CommandEvent) {
        val args = event.args.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        if (args.isEmpty()) {
            throw IllegalArgumentException("Command must contain pattern")
        }

        val patternString = args.joinToString(" ")
        val pattern = patternString.toRegex()

        NameRuleDAO.instance.setPattern(event.guild.id, pattern.pattern).subscribe({
            event.reply("Set username enforcement pattern to `$patternString`")
        }, {
            logger.warn("Could not set username enforcement pattern for guild ${event.guild.id}", it)
            event.replyError("Could not set username enforcement pattern for ${event.guild.name}")
        })
    }

    private fun getPattern(event: CommandEvent) {
        NameRuleDAO.instance.getGuild(event.guild.id).subscribe({
            event.reply("Current username pattern: `${it.pattern}`")
        }, {
            if (it is NoSuchElementException) {
                event.reply("There is no username enforcement pattern set")
            } else {
                logger.warn("Could not get username enforcement pattern for guild ${event.guild.id}", it)
                event.replyError("Could not get username enforcement pattern for ${event.guild.name}")
            }
        })
    }
}
