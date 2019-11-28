package com.dongtronic.diabot.platforms.discord.commands.admin.username

import com.dongtronic.diabot.platforms.discord.commands.DiabotCommand
import com.dongtronic.diabot.data.AdminDAO
import com.jagrosh.jdautilities.command.Command
import com.jagrosh.jdautilities.command.CommandEvent
import org.slf4j.LoggerFactory

class AdminUsernamePatternCommand(category: Command.Category, parent: Command?) : DiabotCommand(category, parent) {

    private val logger = LoggerFactory.getLogger(AdminUsernamePatternCommand::class.java)

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

        AdminDAO.getInstance().setUsernamePattern(event.guild.id, pattern.pattern)

        event.reply("Set username enforcement pattern to `$patternString`")
    }

    private fun getPattern(event: CommandEvent) {
        val pattern = AdminDAO.getInstance().getUsernamePattern(event.guild.id)

        event.reply("Current username pattern: `$pattern`")
    }
}
